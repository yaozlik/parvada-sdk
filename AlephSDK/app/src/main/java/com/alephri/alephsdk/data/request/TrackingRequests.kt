package com.alephri.alephsdk.data.request

import com.google.gson.annotations.SerializedName

data class LocationRequest(
    var id: String,
    var email: String,
    var lat: Double?,
    var lon: Double?,
    var datetime: String,
    var precision: Double,
    var indicators: Indicators
)

data class Indicators(
    var batteryLevel: Float,
    var model: String,
    var appVersion: String,
    var deviceSpeed: Float,
)

data class AuthorizationDetailsResponse (
    @SerializedName("id") var id : String?,
    @SerializedName("title") var title : String?,
    @SerializedName("description") var description : String?,
    @SerializedName("monitorists") var monitorists : List<Monitorist>?,
    @SerializedName("startDate") var startDate : String?,
    @SerializedName("endDate") var endDate : String?,
    @SerializedName("startHour") var startHour : String?,
    @SerializedName("endHour") var endHour : String?,
    @SerializedName("daysOfWeek") var daysOfWeek : List<Int>?
)

data class Monitorist (
    @SerializedName("name") var name : String?,
    @SerializedName("email") var email : String?,
    @SerializedName("avatar") var avatar : String?,
    @SerializedName("id") var id : String?,
    @SerializedName("department") var department : String?,
    @SerializedName("job_title") var jobTitle : String?
)

