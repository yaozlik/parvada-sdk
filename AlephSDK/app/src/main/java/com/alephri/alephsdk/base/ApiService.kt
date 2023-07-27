package com.alephri.alephsdk.base

import com.alephri.alephsdk.data.request.AuthorizationDetailsResponse
import com.alephri.alephsdk.data.request.LocationRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("parvada/readings")
    suspend fun submitLocation(
        @HeaderMap headers: Map<String, String>,
        @Body request: LocationRequest
    ): Response<ResponseBody>

    @GET("authorization/{trackingId}")
    suspend fun getAuthorizationDetails(
        @HeaderMap headers: Map<String, String>,
        @Path("trackingId") trackingId: String,
    ): Response<AuthorizationDetailsResponse>
}
