package com.ebookfrenzy.poe_ebird

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.core.constants.Constants
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.observable.eventdata.MapLoadingErrorEventData
import com.mapbox.maps.extension.style.expressions.dsl.generated.eq
import com.mapbox.maps.extension.style.expressions.dsl.generated.get
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.literal
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.FillExtrusionLayer
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSource

import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener
import com.mapbox.maps.plugin.gestures.gestures
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Dispatcher
import okhttp3.internal.notify
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Map.newInstance] factory method to
 * create an instance of this fragment.
 */
class Map : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var currentMarker: PointAnnotation? = null
    private var birdMarker: PointAnnotation? = null
    private lateinit var mapView: MapView
    lateinit var zoomInButton: Button
    lateinit var zoomOutButton: Button
    lateinit var getDirection: Button
    private lateinit var pointAnnotationManager: PointAnnotationManager
    val markers = mutableListOf<PointAnnotation>()
    var lastClicked: Point? = null
    lateinit var directionInfoCard: CardView
    lateinit var routeDistanceTextView: TextView
    lateinit var routeTimeTextView: TextView
    lateinit var save: Button
    var names: List<String>? = null
    var speciesCode: List<String>? = null
    var location: String? = null
    val model = globalModel
    var locImage: Bitmap? = null
    var placeName: String? = null
    lateinit var frag: Slider
    lateinit var fragManager: FragmentManager
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var home:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)


        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        zoomInButton = view.findViewById(R.id.zoonInButton)
        zoomOutButton = view.findViewById(R.id.zoomOutButton)
        getDirection = view.findViewById(R.id.btnDirections)
        directionInfoCard = view.findViewById(R.id.directionInfoCard)
        routeDistanceTextView = view.findViewById(R.id.routeDistanceTextView)
        routeTimeTextView = view.findViewById(R.id.routeTimeTextView)
        mapView = view.findViewById(R.id.mapView)
        save = view.findViewById(R.id.btnSave)
        home=view.findViewById(R.id.btnHome)

        val mapboxMap: MapboxMap = mapView.getMapboxMap()
        val cameraOptions = CameraOptions.Builder()
        val gesturesPlugin = mapView.gestures
        // Get the `cameraState.center`

        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            // The map is ready; set the camera options here
            val cameraOptions = CameraOptions.Builder()
                .center(Point.fromLngLat(-30.5, 28.0)) // Set center to desired coordinates
                .zoom(3.0) // Set an appropriate zoom level
                .pitch(0.0)
                .bearing(0.0)
                .build()

            // Set the camera options on the map
            mapView.getMapboxMap().setCamera(cameraOptions)
        }

        checkLocationPermission()

// Enable pinch-to-zoom
        gesturesPlugin.updateSettings {

            pitchEnabled = true
        }

        zoomInButton.setOnClickListener {
            val currentZoom = mapView.getMapboxMap().cameraState.zoom
            val center = mapView.getMapboxMap().cameraState.center
            mapboxMap.setCamera(cameraOptions.zoom(currentZoom + 1.0).center(center).build())


            Log.i("zoom", "zoom button is clicked zoom is $currentZoom")
        }

        zoomOutButton.setOnClickListener {
            val currentZoom = mapView.getMapboxMap().cameraState.zoom
            val center = mapView.getMapboxMap().cameraState.center
            mapboxMap.setCamera(cameraOptions.zoom(currentZoom - 1.0).center(center).build())
            Log.i("zoom out", "zoom out is clicked zoom is $currentZoom")
        }
        getDirection.setOnClickListener {
            if (lastClicked != null) {
                getRoute(currentMarker!!.point, lastClicked!!)
            } else {
                Toast.makeText(this.context, "No location has been set", Toast.LENGTH_SHORT).show()
            }
        }
        save.setOnClickListener {
            if (lastClicked != null) {

                CoroutineScope(Dispatchers.IO).launch {
                    reverseGeocode(lastClicked!!)
                }

                if (model.observationList.map { it.observationNo }.isEmpty()) {
                    model.observationList.add(
                        ObservationData(
                            1,
                            names,
                            speciesCode,
                            location!!,
                            lastClicked!!,
                            model.loggedInUser

                        )
                    )
                    Log.i("ObservationList", model.observationList.toString())
                    CoroutineScope(Dispatchers.IO).launch {
                        for (obs in model.observationList) {
                            Log.i("Obs", obs.toString())
                            if (obs != null) {
                                if (model.recyclerData.isEmpty()) {
                                    val maxLength = maxOf(obs.name!!.size, obs.SpeciesCode!!.size)
                                    for (index in 0 until maxLength) {
                                        val name = obs.name.getOrNull(index)!!// Safely access name
                                        val speciesCode = obs.SpeciesCode.getOrNull(index)!!

                                        model.recyclerData.add(
                                            ObservationRecyclerData(
                                                1,
                                                name,
                                                location!!,
                                                getBirdImageUrl(name)
                                            )
                                        )
                                    }

                                    Log.i("Recycler Data", model.recyclerData.toString())
                                } else {
                                    val maxLength = maxOf(obs.name!!.size, obs.SpeciesCode!!.size)
                                    for (index in 0 until maxLength) {
                                        val name = obs.name.getOrNull(index)!!// Safely access name
                                        val speciesCode = obs.SpeciesCode.getOrNull(index)!!

                                        model.recyclerData.add(
                                            ObservationRecyclerData(
                                                model.recyclerData.map { it.recyclerNo }.lastIndex + 1,
                                                name,
                                                location!!,
                                                getBirdImageUrl(name)
                                            )
                                        )
                                    }

                                }
                                Log.i("Recycler Data", model.recyclerData.toString())
                            }
                        }

                        withContext(Dispatchers.Main) {
                            var obs=Observations()
                            if(obs.observationAdapter!=null) {
                                obs.observationAdapter!!.notifyDataSetChanged() // Notify the adapter about data changes
                            }
                            Toast.makeText(this@Map.context, "Successfully saved", Toast.LENGTH_SHORT).show()
                        }

                    }
                } else {
                    model.observationList.add(
                        ObservationData(
                            (model.observationList.map { it.observationNo }.lastIndex) + 1,
                            names,
                            speciesCode,
                            location!!,
                            lastClicked!!,
                            model.loggedInUser
                        )
                    )
                    Log.i("ObservationList", model.observationList.toString())
                    CoroutineScope(Dispatchers.IO).launch {
                        for (obs in model.observationList) {
                            Log.i("Obs", obs.toString())
                            if (obs != null) {
                                if (model.recyclerData.isEmpty()) {
                                    val maxLength = maxOf(obs.name!!.size, obs.SpeciesCode!!.size)
                                    for (index in 0 until maxLength) {
                                        val name = obs.name.getOrNull(index)!!// Safely access name
                                        val speciesCode = obs.SpeciesCode.getOrNull(index)!!

                                        model.recyclerData.add(
                                            ObservationRecyclerData(
                                                1,
                                                name,
                                                location!!,
                                                getBirdImageUrl(name)
                                            )
                                        )

                                    }
                                    Log.i("Recycler Data", model.recyclerData.toString())
                                } else {
                                    val maxLength = maxOf(obs.name!!.size, obs.SpeciesCode!!.size)
                                    for (index in 0 until maxLength) {
                                        val name = obs.name.getOrNull(index)!!// Safely access name
                                        val speciesCode = obs.SpeciesCode.getOrNull(index)!!

                                        model.recyclerData.add(
                                            ObservationRecyclerData(
                                                model.recyclerData.map { it.recyclerNo }.lastIndex + 1,
                                                name,
                                                location!!,
                                                getBirdImageUrl(name)
                                            )
                                        )
                                    }


                                }
                                Log.i("Recycler Data", model.recyclerData.toString())
                            }
                        }
                        withContext(Dispatchers.Main) {
                            var obs=Observations()
                            if(obs.observationAdapter!=null) {
                                obs.observationAdapter!!.notifyDataSetChanged() // Notify the adapter about data changes
                            }
                            Toast.makeText(this@Map.context, "Successfully saved", Toast.LENGTH_SHORT).show()
                        }
                    }

                }


            } else {
                Toast.makeText(this.context, "No location has been set", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.bird_marker)

        val targetWidth = 90
        val targetHeight = 90

        // Resize the Bitmap using createScaledBitmap
        val resizedBitmap =
            Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->
            style.addImage(
                "default-marker",
                BitmapFactory.decodeResource(resources, R.drawable.red_marker)
            )
            mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->
                style.addImage(
                    "secondary-marker",
                    resizedBitmap
                )
            }
            // Initialize the annotation manager after the style has been loaded
            val annotationApi = mapView.annotations
            pointAnnotationManager = annotationApi.createPointAnnotationManager()
            mapView.gestures.addOnMapLongClickListener { point ->
                // Add a marker at the clicked location
                if (currentMarker != null) {
                    // Remove the current marker
                    pointAnnotationManager.delete(currentMarker!!)
                    currentMarker = null // Reset the reference
                }

                addMarker(point)
                mapboxMap.setCamera(cameraOptions.center(point).zoom(12.0).build())
                Log.i("point", point.toString())
                lifecycleScope.launch {
                    fetchRecentObservations("ga9bp34gmq1l", point.latitude(), point.longitude())
                }
                true // Return true to indicate that the long click was handled
            }


        }
        home.setOnClickListener {
            var homekt=Home()
            frag=Slider()
            fragManager= requireFragmentManager()
            val fragTrans=fragManager.beginTransaction()
            fragTrans.replace(R.id.frameLayout,frag)
            fragTrans.commit()
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }


    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    private fun addMarker(point: Point) {
        // Create an annotation (marker) at the long-clicked location

        directionInfoCard.visibility = View.INVISIBLE
        val pointAnnotationOptions = PointAnnotationOptions()
            .withPoint(point)
            .withIconImage("default-marker")
            .withIconSize(0.75) // Adjust the size as needed

        // Add the annotation to the map
        currentMarker = pointAnnotationManager.create(pointAnnotationOptions)
        mapView.getMapboxMap().getStyle { style ->
            if (style.getLayer("line-layer-id") != null) {
                style.removeStyleLayer("line-layer-id")  // Remove the existing line layer
            }
            if (style.getSource("line-source-id") != null) {
                style.removeStyleSource("line-source-id")  // Remove the associated source if needed
            }
        }
    }

    suspend fun fetchRecentObservations(apiKey: String, lat: Double, lon: Double) {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }

        try {
            // Build the URL
            val url = "https://api.ebird.org/v2/data/obs/geo/recent"
            val response: String = client.get(url) {
                parameter("lat", lat) // Latitude
                parameter("lng", lon) // Longitude
                parameter("maxResults", 5) // Maximum number of results
                header("X-eBirdApiToken", apiKey)
            }.bodyAsText()

            // Parse the JSON response
            val observations: List<Observation> = Json.decodeFromString(response)

            // Handle the observations
            for (obs in observations) {
                println(
                    "Species: ${obs.comName}, How Many: ${obs.howMany}, Latitude: ${obs.lat}, Longitude: ${obs.lng}, " +
                            "Location ID: ${obs.locId}, Location Name: ${obs.locName}, Location Private: ${obs.locationPrivate}, " +
                            "Date: ${obs.obsDt}, Observed Reviewed: ${obs.obsReviewed}, Observation Valid: ${obs.obsValid}, " +
                            "Scientific Name: ${obs.sciName}, Species Code: ${obs.speciesCode}, Submission ID: ${obs.subId}, exoticCategory: ${obs.exoticCategory} "
                )
            }
            addNearbyBirdMarkers(observations)

        } catch (e: Exception) {
            println("Error: ${e.message}")
        } finally {
            client.close()
        }
    }

    private fun addNearbyBirdMarkers(observations: List<Observation>) {

// Remove previous markers
        markers.forEach { marker ->
            pointAnnotationManager.delete(marker)
        }
        markers.clear() // Clear the list after removing all markers


        observations.forEachIndexed { index, observation ->


            val groupedObservations = observations.groupBy { Pair(it.lat, it.lng) }

// Create markers with grouped observation data
            groupedObservations.forEach { (coordinates, obsList) ->
                // Create a location point from the group's coordinates
                val location = Point.fromLngLat(coordinates.second, coordinates.first)

                // Attach a JSON object with the list of observation names
                val pointAnnotationOptions = PointAnnotationOptions()
                    .withPoint(location)
                    .withIconImage("secondary-marker")
                    .withIconSize(1.5) // Adjust the size as needed


                    .withTextSize(12.0) // Adjust text size if needed
                    .withTextOffset(listOf(0.0, -2.0))
                    .withIconOffset(listOf(0.0))
                    .withData(JsonObject().apply {
                        add("names", JsonArray().apply {
                            obsList.forEach { add(it.comName) }

                        })
                        add("location", JsonArray().apply {
                            obsList.forEach { add(it.locName) }
                        })
                        add("speciesCode", JsonArray().apply {
                            obsList.forEach { add(it.speciesCode) }
                        })
                    })


                // Log each marker creation
                Log.d(
                    "MarkerDebug",
                    "Adding marker #${index + 1} at Lat=${observation.lat}, Lng=${observation.lng}"
                )

                // Add the annotation to the map
                val birdMarker = pointAnnotationManager.create(pointAnnotationOptions)
                markers.add(birdMarker) // Add the newly created marker to the list


            }
        }
        val tolerance = 0.00001
        pointAnnotationManager.addClickListener { annotation ->
            // Show callout or handle the click event
            val data = annotation.getData()?.asJsonObject
            Log.i("data", data.toString())
            names = data?.getAsJsonArray("names")?.mapNotNull { it.asString } ?: listOf("Unknown")
            speciesCode =
                data?.getAsJsonArray("speciesCode")?.mapNotNull { it.asString } ?: listOf("Unknown")
            location = data?.getAsJsonArray("location")?.mapNotNull { it.asString }?.getOrNull(0)
                ?: "Unknown location"
            Log.i("Names", names.toString())
            Log.i("Species Code", speciesCode.toString())
            Log.i("Location", location.toString())

            lastClicked = annotation.point


            // Format the names to display in the popup
            val formattedTitles = names!!.joinToString(separator = "\n") { "Name: $it" }

            // Show popup with all observation names
            showPopup(annotation.point!!, formattedTitles)


            true // Return true if the event was handled
        }

    }

    private fun showPopup(point: Point, title: String) {
        this.context?.let {
            AlertDialog.Builder(it)
                .setTitle("Marker Information")
                .setMessage(title)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun getRoute(origin: Point, destination: Point) {
        val routeOptions = RouteOptions.builder()
            .coordinatesList(listOf(origin, destination))
            .profile(DirectionsCriteria.PROFILE_DRIVING)  // Choose driving, walking, cycling, etc.
            .build()

        MapboxDirections.builder()
            .routeOptions(routeOptions)
            .accessToken(getString(R.string.mapbox_access_token))
            .build()
            .enqueueCall(object : Callback<DirectionsResponse> {
                override fun onResponse(
                    call: Call<DirectionsResponse>,
                    response: Response<DirectionsResponse>
                ) {
                    val route = response.body()?.routes()?.firstOrNull()
                    if (route != null) {
                        drawRoute(route)
                        // Extract the total distance and duration
                        val totalDistanceMeters = route.distance() // Distance in meters
                        val totalDurationSeconds = route.duration() // Duration in seconds

                        // Convert distance to kilometers and duration to minutes
                        val totalDistanceKm = totalDistanceMeters / 1000
                        val totalDurationMinutes = totalDurationSeconds / 60

                        // Update the TextViews with distance and time
                        routeDistanceTextView.text = "Distance: %.2f km".format(totalDistanceKm)
                        routeTimeTextView.text = "Time: %.0f min".format(totalDurationMinutes)

                        // Show the small box
                        directionInfoCard.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                    // Handle failure
                }
            })
    }

    private fun drawRoute(route: DirectionsRoute) {
        val lineString = LineString.fromPolyline(
            route.geometry() ?: "",
            Constants.PRECISION_6
        )

        val lineLayer = LineLayer("line-layer-id", "line-source-id").apply {


            lineColor(Color.RED)
            lineWidth(5.0) // Set line width
            lineCap(LineCap.ROUND) // Set line cap style
            lineJoin(LineJoin.ROUND) // Set line join style

        }


        // Create the GeoJsonSource with the data
        val geoJsonSource = geoJsonSource("line-source-id") {
            geometry(lineString)
        }

        mapView.getMapboxMap().getStyle { style ->
            if (style.getLayer("line-layer-id") != null) {
                style.removeStyleLayer("line-layer-id")  // Remove the existing line layer
            }
            if (style.getSource("line-source-id") != null) {
                style.removeStyleSource("line-source-id")  // Remove the associated source if needed
            }
            style.addSource(geoJsonSource)
            style.addLayer(lineLayer)
        }
    }

    suspend fun reverseGeocode(point: Point) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.mapbox.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val mapboxService = retrofit.create(MapboxService::class.java)
        val response = mapboxService.getReverseGeocode(
            point.longitude(),
            point.latitude(),
            "YOUR_MAPBOX_ACCESS_TOKEN"
        )

        if (response.isSuccessful) {
            val geocodingResponse = response.body()
            geocodingResponse?.features?.firstOrNull()?.let { feature ->
                placeName = feature.place_name // Full place name (e.g., "City, Suburb")
                feature.context?.forEach { context ->
                    if (context.id.contains("place")) {
                        val cityName = context.text
                        println("City: $cityName")
                    } else if (context.id.contains("neighborhood")) {
                        val suburbName = context.text
                        println("Suburb: $suburbName")
                    }
                }
            }
        }
    }




    suspend fun getBirdImageUrl(speciesName: String): String? {
        return withContext(Dispatchers.IO) {
            var imageUrl: String? = null
            try {
                // Format the species name for the search query
                val formattedSpeciesName = speciesName.replace(" ", "+")
                val searchUrl = "https://www.namahariplaasmark.com/search?q=$formattedSpeciesName"
                // Log the search URL
                Log.i("Search URL", searchUrl)

                // Fetch and parse the HTML document from the search URL
                val document = Jsoup.connect(searchUrl).get()
                // Log the title of the fetched document
                Log.i("Document Title", document.title())

                // Iterate through all post-title-container divs to find the correct one
                val postTitleContainers = document.select("div.post-title-container")
                Log.i("Post Containers Count", postTitleContainers.size.toString())
                if(postTitleContainers.size==0){
                    imageUrl=getAnotherBirdImageUrl(speciesName)
                }
                for (container in postTitleContainers) {
                    val anchorElement = container.select("a[href]").first()

                    // Check if anchor text or content matches the speciesName
                    val textMatch = container.text().contains(speciesName, ignoreCase = true)

                    // If a matching text and valid href is found, return the URL
                    if (textMatch && anchorElement != null && anchorElement.attr("href")
                            .startsWith("http")
                    ) {

                        val postUrl = anchorElement.attr("href")

                        // Now fetch the post URL to get the image
                        val postDocument = Jsoup.connect(postUrl).get()

                        // Log the post URL and title
                        Log.i("Post URL", postUrl)
                        Log.i("Post Document Title", postDocument.title())

                        // Select the image URL from the post
                        val imgElement = postDocument.select("div.separator img").first()
                        if (imgElement != null) {
                            imageUrl = imgElement.attr("src") // Get the src attribute of the image
                            Log.i("Image URL", imageUrl)
                        }else {
                            Log.e("Error", "Image element not found in post: $postUrl, $imgElement")
                        }
                        break // Exit the loop after finding the first matching species
                    }
                }


            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Error", e.message.toString())
            }

            imageUrl // Return the image URL or null if not found
        }
    }


    suspend fun getAnotherBirdImageUrl(speciesName: String): String? {
        return withContext(Dispatchers.IO) {
            var imageUrl: String? = null
            try {
                // Format the species name for the search query
                val formattedSpeciesName = speciesName.replace(" ", "+")
                val searchUrl = "https://observation.org/search/?q=$formattedSpeciesName"
                Log.i("Search URL", searchUrl)

                // Fetch and parse the HTML document from the search URL
                val document = Jsoup.connect(searchUrl).get()
                Log.i("Document Title", document.title())

                // Find the sub URL for the species (inside <li class="lead"><a href="...">)
                val speciesAnchor = document.select("ul li.lead a[href]").firstOrNull {
                    it.text().contains(speciesName, ignoreCase = true)
                }

                // If a matching species link is found, proceed to get the image
                if (speciesAnchor != null) {
                    val speciesPageUrl = "https://observation.org" + speciesAnchor.attr("href")
                    Log.i("Species Page URL", speciesPageUrl)

                    // Now fetch the species page to extract the image
                    val speciesDocument = Jsoup.connect(speciesPageUrl).get()
                    val imageElement = speciesDocument.select("a.lightbox-gallery-image img").first()

                    // Extract the image URL
                    if (imageElement != null) {
                        imageUrl = "https://observation.org" + imageElement.attr("src").split("?")[0]
                        Log.i("Image URL", imageUrl)
                    } else {
                        Log.e("Error", "Image element not found on species page: $speciesPageUrl")
                    }
                } else {
                    Log.e("Error", "Species link not found for: $speciesName")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Error", e.message.toString())
            }
            imageUrl // Return the image URL or null if not found
        }

    }

    fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted; request it

                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )

        } else {
            // Permission is already granted; you can proceed with using location
            getUserLocation()
        }
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with accessing user location
                    getUserLocation()
                } else {
                    // Permission denied; show a message to the user explaining why location is needed
                    Toast.makeText(this.requireContext(), "Permission denied ", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    fun getUserLocation() {
        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())

        // Get last known location

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Handle the location object
                if (location != null) {
                    // Use the location (latitude and longitude)
                    val latitude = location.latitude
                    val longitude = location.longitude
                    // Update your map with the user’s location here
                    addMarker(Point.fromLngLat(latitude,longitude))
                    lifecycleScope.launch {
                        fetchRecentObservations("ga9bp34gmq1l", latitude, longitude)
                    }
                    Toast.makeText(this.requireContext(), "Default location", Toast.LENGTH_SHORT).show()
                }else {
                    Toast.makeText(this.requireContext(), "Location is null", Toast.LENGTH_SHORT)
                        .show()
                    }
            }
    }








    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Map.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Map().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}