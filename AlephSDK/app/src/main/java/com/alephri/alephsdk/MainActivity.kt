package com.alephri.alephsdk

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.alephri.alephsdk.data.base.State
import com.alephri.alephsdk.facade.AlephSDK
import com.aleprhi.alephSDK.type.GeoPointInput
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var loginButton: Button
    lateinit var notificationByIdButton: Button
    lateinit var notificationsButton: Button
    lateinit var riskRouteButton: Button

    val alephSDK = AlephSDK(this, apiKey = "1010101010101")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loginButton = findViewById(R.id.loginBtn)
        notificationByIdButton = findViewById(R.id.notificationByIdBtn)
        notificationsButton = findViewById(R.id.notificationsBtn)
        riskRouteButton = findViewById(R.id.riskRouteButton)
    }

    override fun onResume() {
        super.onResume()
        loginButton.setOnClickListener {
            MainScope().launch {
                alephSDK.login(email = "ofernandez@alephri.com", password = "password").collect {
                    when (it) {
                        is State.Success -> {
                            Log.d("Success", "Token ${it.data}")
                        }
                        is State.Failure -> {
                            Log.d("Failure", "Error ${it.exception}")
                        }
                        is State.Progress -> {
                            Log.d("Progress", "Is Loading")
                        }
                    }
                }
            }
        }
        notificationByIdButton.setOnClickListener {
            MainScope().launch {
                alephSDK.getNotificationById(239764).collect {
                    when (it) {
                        is State.Success -> {
                            Log.d("Success", "Notification ${it.data}")
                        }
                        is State.Failure -> {
                            Log.d("Failure", "Error ${it.exception}")
                        }
                        is State.Progress -> {
                            Log.d("Progress", "Is Loading")
                        }
                    }
                }
            }
        }

        notificationsButton.setOnClickListener {
            MainScope().launch {
                alephSDK.getNotifications(10, offset = 0).collect {
                    when (it) {
                        is State.Success -> {
                            Log.d("Success", "Notifications ${it.data.size}")
                        }
                        is State.Failure -> {
                            Log.d("Failure", "Error ${it.exception}")
                        }
                        is State.Progress -> {
                            Log.d("Progress", "Is Loading")
                        }
                    }
                }
            }
        }

        riskRouteButton.setOnClickListener {
            MainScope().launch {
                alephSDK.getRoute(
                    GeoPointInput(lat = 19.3216323, lon = -99.1790659),
                    GeoPointInput(lat = 19.3215333, lon =  -99.1790659)
                ).collect {
                    when (it) {
                        is State.Success -> {
                            Log.d("Success", "Router ${it.data}")
                        }
                        is State.Failure -> {
                            Log.d("Failure", "Error ${it.exception}")
                        }
                        is State.Progress -> {
                            Log.d("Progress", "Is Loading")
                        }
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AlephSDK.PERMISSION_REQUEST_ACCESS_LOCATION) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                alephSDK.requestAlwaysPermissions()
            }
        }
    }
}