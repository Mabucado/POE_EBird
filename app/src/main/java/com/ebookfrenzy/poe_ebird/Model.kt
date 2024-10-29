package com.ebookfrenzy.poe_ebird
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.geojson.Point
import kotlinx.serialization.Serializable

class Model (val usersList:MutableList<Users> = mutableListOf(),
             val observationList:MutableList<ObservationData> = mutableListOf(),
             val feedbackList:MutableList<Feedback> = mutableListOf(),
             val recyclerData:MutableList<ObservationRecyclerData> = mutableListOf(),
            var loggedInUser:String? =null
){
}
data class Users(
    val username: String,
    val name: String,
    val surname: String,
    val email:String,
    var unit: String?=null,
    var profile_image:Uri?=null,
    val password: String
)
data class ObservationData(
    val observationNo: Int,
    val name: List<String>?,
    val SpeciesCode : List<String>?,
    val location:String,
    val coordinates: Point,
    val username:String?


    )
data class ObservationRecyclerData(
    val recyclerNo:Int,
    val name: String,
    val location :String,
    var image: String?,
)

@Serializable
data class Observation(
    val comName: String,
    val howMany: Int,
    val lat: Double,
    val lng: Double,
    val locId: String,
    val locName: String,
    val locationPrivate: Boolean,
    val obsDt: String,
    val obsReviewed: Boolean,
    val obsValid: Boolean,
    val sciName: String,
    val speciesCode: String,
    val subId: String,
    val exoticCategory: String?=null
)
data class Feedback(
    val feedbackID:Int,
    val message: String,
    val often: String,
    val motivate:String,
    val improve:String,
    val date: String,
    val username: String
)
data class PlacesResponse(
    val candidates: List<Candidate>
)

data class Candidate(
    val place_id: String,
    val photos: List<String>?
)
data class GeocodingResponses(
    val features: List<Feature>
)

data class Feature(
    val place_name: String,
    val context: List<Context>?
)
data class Context(
    val id: String,
    val text: String
)
val globalModel = Model()
fun saveModelDataToFirestore(model: Model) {
    val db = FirebaseFirestore.getInstance()

    // Directly convert the lists and properties to a Firestore-compatible format
    val modelData = hashMapOf(
        "usersList" to model.usersList.takeIf { it.isNotEmpty() }?.map { user ->
            hashMapOf(
                "username" to user.username,
                "name" to user.name,
                "surname" to user.surname,
                "email" to user.email,
                "unit" to user.unit,
                "profile_image" to user.profile_image?.toString(), // Convert Uri to String
                "password" to user.password
            )
        },
        "observationList" to model.observationList.takeIf { it.isNotEmpty() }?.map { observation ->
            hashMapOf(
                "observationNo" to observation.observationNo,
                "name" to observation.name,
                "SpeciesCode" to observation.SpeciesCode,
                "location" to observation.location,
                "coordinates" to hashMapOf(
                    "latitude" to observation.coordinates.latitude(),
                    "longitude" to observation.coordinates.longitude()
                ),
                "username" to observation.username
            )
        },
        "feedbackList" to model.feedbackList.takeIf { it.isNotEmpty() }?.map { feedback ->
            hashMapOf(
                "feedbackID" to feedback.feedbackID,
                "message" to feedback.message,
                "often" to feedback.often,
                "motivate" to feedback.motivate,
                "improve" to feedback.improve,
                "date" to feedback.date,
                "username" to feedback.username
            )
        },
        "recyclerData" to model.recyclerData.takeIf { it.isNotEmpty() }?.map { recycler ->
            hashMapOf(
                "recyclerNo" to recycler.recyclerNo,
                "name" to recycler.name,
                "location" to recycler.location,
                "image" to recycler.image
            )
        },
        "loggedInUser" to model.loggedInUser
    )
    if(model.loggedInUser==null){
        var user=model.usersList.map { it.username }.get(0)
        // Add the data to Firestore
        db.collection("models").document(user)
            .set(modelData)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "Document successfully written!")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding document", e)
            }
    }
    else{ // Add the data to Firestore
        db.collection("models").document(model.loggedInUser!!)
            .set(modelData)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "Document successfully written!")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding document", e)
            }}

}
fun retrieveModelDataFromFirestore(
    loggedInUser: String,
    onDataRetrieved: (Model) -> Unit,
    onError: (Exception) -> Unit
) {
    retrieveAllUsers({ usersList ->
        // After retrieving all users, retrieve the model data
        val db = FirebaseFirestore.getInstance()

        db.collection("models").document(loggedInUser)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    // Create an instance of Model with populated lists
                    val model = Model(
                        usersList = usersList.toMutableList(), // Use the retrieved users list
                        observationList = (document.get("observationList") as? List<kotlin.collections.Map<String, Any?>> ?: emptyList())
                            .map { observationMap ->
                                ObservationData(
                                    observationNo = observationMap["observationNo"] as Int,
                                    name = observationMap["name"] as? List<String> ?: emptyList(),
                                    SpeciesCode = observationMap["SpeciesCode"] as? List<String> ?: emptyList(),
                                    location = observationMap["location"] as String,
                                    coordinates = Point.fromLngLat(
                                        (observationMap["coordinates"] as kotlin.collections.Map<String, Any?>)["latitude"] as Double,
                                        (observationMap["coordinates"] as kotlin.collections.Map<String, Any?>)["longitude"] as Double
                                    ),
                                    username = observationMap["username"] as? String
                                )
                            }.toMutableList(),
                        feedbackList = (document.get("feedbackList") as? List<kotlin.collections.Map<String, Any?>> ?: emptyList())
                            .map { feedbackMap ->
                                Feedback(
                                    feedbackID = feedbackMap["feedbackID"] as Int,
                                    message = feedbackMap["message"] as String,
                                    often = feedbackMap["often"] as String,
                                    motivate = feedbackMap["motivate"] as String,
                                    improve = feedbackMap["improve"] as String,
                                    date = feedbackMap["date"] as String,
                                    username = feedbackMap["username"] as String
                                )
                            }.toMutableList(),
                        recyclerData = (document.get("recyclerData") as? List<kotlin.collections.Map<String, Any?>> ?: emptyList())
                            .map { recyclerMap ->
                                ObservationRecyclerData(
                                    recyclerNo = recyclerMap["recyclerNo"] as Int,
                                    name = recyclerMap["name"] as String,
                                    location = recyclerMap["location"] as String,
                                    image = recyclerMap["image"] as? String
                                )
                            }.toMutableList(),
                        loggedInUser = loggedInUser // Assign the logged in user
                    )

                    // Call the onDataRetrieved callback with the populated model
                    onDataRetrieved(model)
                } else {
                    onError(Exception("No document found for user: $loggedInUser"))
                }
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }) { exception ->
        // Handle any errors while retrieving users
        onError(exception)
    }
}

fun retrieveAllUsers(onUsersRetrieved: (List<Users>) -> Unit, onError: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    // Retrieve all documents from the "models" collection
    db.collection("models")
        .get()
        .addOnSuccessListener { querySnapshot ->
            // Create a mutable list to hold all users
            val allUsersList = mutableListOf<Users>()

            // Iterate through each document in the models collection
            for (document in querySnapshot.documents) {
                document.data?.let { modelData ->
                    // Get usersList from each model document
                    val usersList = modelData["usersList"] as? List<kotlin.collections.Map<String, Any?>> ?: emptyList()

                    // Map usersList to Users objects and add to allUsersList
                    for (userData in usersList) {
                        (userData as? kotlin.collections.Map<String, Any?>)?.let { userMap ->
                            // Use safe casting to avoid NullPointerException
                            val username = userMap["username"] as? String
                            val name = userMap["name"] as? String
                            val surname = userMap["surname"] as? String
                            val email = userMap["email"] as? String
                            val unit = userMap["unit"] as? String
                            val profileImageUri = userMap["profile_image"]?.let { Uri.parse(it as String) }
                            val password = userMap["password"] as? String

                            // Check for required fields before creating a Users object
                            if (username != null && name != null && surname != null && email != null && password != null) {
                                allUsersList.add(
                                    Users(
                                        username = username,
                                        name = name,
                                        surname = surname,
                                        email = email,
                                        unit = unit,
                                        profile_image = profileImageUri,
                                        password = password
                                    )
                                )
                                globalModel.usersList.addAll(allUsersList)
                            }
                        }
                    }
                }
            }

            // Call the callback with the aggregated users list
            onUsersRetrieved(allUsersList)
        }
        .addOnFailureListener { e ->
            onError(e)
        }
}




