package com.ebookfrenzy.poe_ebird

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MapboxService {
    @GET("geocoding/v5/mapbox.places/{longitude},{latitude}.json")
    suspend fun getReverseGeocode(
        @Path("longitude") longitude: Double,
        @Path("latitude") latitude: Double,
        @Query("access_token") accessToken: String
    ): Response<GeocodingResponses>
}