package com.ebookfrenzy.poe_ebird

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaceService {
    @GET("place/findplacefromtext/json")
    fun getPlaceDetails(
        @Query("input") locationName: String,
        @Query("inputtype") inputType: String = "textquery",
        @Query("fields") fields: String = "photos,place_id",
        @Query("key") apiKey: String
    ): Call<PlacesResponse>
}