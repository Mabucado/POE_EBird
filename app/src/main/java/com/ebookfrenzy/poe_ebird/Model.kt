package com.ebookfrenzy.poe_ebird
import android.graphics.Bitmap
import android.net.Uri
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
    val unit: String?=null,
    val profile_image:Uri?=null,
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