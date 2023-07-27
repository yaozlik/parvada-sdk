package com.alephri.alephsdk.facade

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import com.alephri.alephsdk.data.base.State
import com.alephri.alephsdk.data.entity.Notification
import com.alephri.alephsdk.data.react.ReadableMap
import com.alephri.alephsdk.data.request.Indicators
import com.alephri.alephsdk.data.request.LocationRequest
import com.alephri.alephsdk.usecase.GetNotificationById
import com.alephri.alephsdk.usecase.GetNotifications
import com.alephri.alephsdk.usecase.GetRiskRoute
import com.alephri.alephsdk.usecase.Login
import com.alephri.alephsdk.usecase.SendReading
import com.aleprhi.alephSDK.GetRiskRouterQuery
import com.aleprhi.alephSDK.type.GeoPointInput
import com.transistorsoft.locationmanager.adapter.BackgroundGeolocation
import com.transistorsoft.locationmanager.adapter.TSConfig
import com.transistorsoft.locationmanager.adapter.callback.TSCallback
import com.transistorsoft.locationmanager.adapter.callback.TSLocationCallback
import com.transistorsoft.locationmanager.location.TSLocation
import kotlinx.coroutines.flow.Flow
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AlephSDK(private val context: Context, private val apiKey: String) {
    companion object {
        const val PERMISSION_REQUEST_ACCESS_LOCATION = 5000
    }

    private fun getHeader(): Map<String, String> {
        val token = "Bearer ${getAlephToken()}"
        return mapOf(Pair("Authorization", token))
    }

    suspend fun login(email: String, password: String): Flow<State<String>> {
        val loginUseCase = Login()
        val flow = loginUseCase.execute(
            Login.Params(
                email = email,
                password = password,
                deviceId = getDeviceId()
            )
        )

        flow.collect {
            when (it) {
                is State.Success -> {
                    val token = it.data
                    val sharedPref = context.getSharedPreferences("ALEPHRI", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("ALEPHRI_TOKEN", token)
                        putString("ALEPHRI_EMAIL", email)
                        apply()
                    }
                    initTracking()
                }
                is State.Failure -> {
                    //Do Nothing
                }
                is State.Progress -> {
                    //Do Nothing
                }
            }
        }
        return flow
    }

    suspend fun getNotificationById(id: Int): Flow<State<Notification>> {
        val getNotificationByIdUseCase = GetNotificationById(getAlephToken())

        return getNotificationByIdUseCase.execute(
            GetNotificationById.Params(
                id = id
            )
        )
    }

    suspend fun getNotifications(
        limit: Int,
        offset: Int
    ): Flow<State<List<Notification>>> {
        val getNotificationsUseCase = GetNotifications(getAlephToken())

        return getNotificationsUseCase.execute(
            GetNotifications.Params(
                limit = limit,
                offset = offset
            )
        )
    }

    suspend fun getRoute(
        origin: GeoPointInput,
        destination: GeoPointInput
    ): Flow<State<GetRiskRouterQuery.RiskRouter>> {
        val getRiskRouteUseCase = GetRiskRoute(getAlephToken())

        return getRiskRouteUseCase.execute(
            GetRiskRoute.Params(
                origin = origin,
                destination = destination
            )
        )
    }

    fun requestAlwaysPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                context as Activity, arrayOf(
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ), PERMISSION_REQUEST_ACCESS_LOCATION
            )
        }
    }

    suspend fun managePushNotification(id: Int): Flow<State<Notification>> {
        return getNotificationById(id)
    }

    @SuppressLint("HardwareIds")
    private fun getDeviceId(): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            context as Activity, arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            ), PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    private fun getISODate(): String {
        val result: String
        val currentDate = Date()
        val outputFormat = SimpleDateFormat.getDateInstance() as SimpleDateFormat
        outputFormat.applyPattern("yyyy-MM-dd'T'HH:mm:ssZ")
        outputFormat.timeZone = TimeZone.getTimeZone("GMT-0")
        result = outputFormat.format(currentDate)
        return result
    }

    private fun getBatteryLevel(): Float {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).toFloat()
    }

    private suspend fun sendReadings(): Flow<State<Boolean>> {
        val sendReadingsUseCase = SendReading()
        val sharedPref = context.getSharedPreferences("ALEPHRI", Context.MODE_PRIVATE)
        val email = sharedPref.getString("ALEPHRI_EMAIL", "") ?: ""

        var indicators = Indicators(
            batteryLevel = getBatteryLevel(),
            model = Build.MODEL,
            appVersion = "SDK 1.0",
            deviceSpeed = 1.0f
        )

        val request = LocationRequest(
            id = getDeviceId(),
            email = email,
            lat = 19.0,
            lon = -99.20,
            datetime = getISODate(),
            precision = 1.0,
            indicators = indicators
        )

        return sendReadingsUseCase.execute(
            SendReading.Params(
                header = getHeader(),
                request = request
            )
        )
    }

    private fun configTracking() {

        val params = ReadableMap()
        params["enableHeadless"] = true
        params["preventSuspend"] = true
        params["showsBackgroundLocationIndicator"] = true
        params["foregroundService"] = true
        params["startOnBoot"] = true
        params["stopOnTerminate"] = false
        params["debug"] = false
        params["stopTimeout"] = 5
        params["distanceFilter"] = 10
        params["desiredAccuracy"] = 100
        params["headlessJobService"] = "AlephService.HeadlessTask"

        val tsConfig = TSConfig.getInstance(context)
        tsConfig.reset()
        tsConfig.updateWithJSONObject(mapToJson(params))

    }

    private fun mapToJson(map: ReadableMap): JSONObject? {
        val iterator = map.iterator()
        val json = JSONObject()
        try {
            while (iterator.hasNext()) {
                val key: String = iterator.next().key
                when (map[key]) {
                    is String -> json.put(key, map.getString(key))
                    is Boolean -> json.put(key, map.getBoolean(key))
                    is Int -> json.put(key, map.getInt(key))
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return json
    }

    private fun initTracking() {
        val callback: TSCallback
        val onLocationCallback: TSLocationCallback
        val onMotionCallback: TSLocationCallback

        configTracking()

        onLocationCallback = object : TSLocationCallback {
            override fun onLocation(location: TSLocation?) {
                Log.d(
                    "Location",
                    "location ${location?.location?.latitude} -- ${location?.location?.longitude}"
                )
                // Aquí va el reading
            }

            override fun onError(p0: Int?) {
                Log.d("Location", "Error")
            }
        }

        onMotionCallback = object : TSLocationCallback {
            override fun onLocation(location: TSLocation?) {
                Log.d(
                    "Motion",
                    "location ${location?.location?.latitude} -- ${location?.location?.longitude}"
                )
            }

            override fun onError(p0: Int?) {
                Log.d("Motion", "Error")
            }
        }

        callback = object : TSCallback {
            override fun onSuccess() {
                Log.d("Configuration Success", "success")
            }

            override fun onFailure(error: String) {
                Log.d("Configuration Error", error)
            }
        }

        BackgroundGeolocation.getInstance(context).ready(
            callback
        )
        BackgroundGeolocation.getInstance(context).start()
        BackgroundGeolocation.getInstance(context).onLocation(onLocationCallback)
        BackgroundGeolocation.getInstance(context).onMotionChange(onMotionCallback)
    }

    private fun getAlephToken(): String {
        val sharedPref = context.getSharedPreferences("ALEPHRI", Context.MODE_PRIVATE)
        return sharedPref.getString("ALEPHRI_TOKEN", "") ?: ""
    }
}
