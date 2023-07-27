package com.alephri.alephsdk.data.base

object ErrorHandler {
    private const val TAG = "ErrorHandler"
    const val NO_LOGGED_MESSAGE = "Please Log in."
    const val LOGIN_ERROR = "Invalid Credentials."
    const val EMPTY_RESPONSE = "Server returned empty response."
    const val NETWORK_ERROR_MESSAGE = "Please check your internet connectivity and try again!"
    const val NO_SUCH_DATA = "Data not found in the database"
    const val NO_SUCH_DATA_MAPBOX = "Route not found in the Mapbox API"
    const val UNKNOWN_ERROR = "An unknown error occurred!"
    const val CONFIRMATION_REQUIRED = "Confirmation required."
}