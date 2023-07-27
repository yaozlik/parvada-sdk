package com.alephri.alephsdk.data.entity

sealed class Notification(val id: Int, val dateTime: String) {
    class Alert(
        id: Int,
        dateTime: String,
        title: String,
        body: String,
        image: String,
        attended: Boolean,
        sender: String,
        source: String,
        origin: String,
        location: String,
        lat: Double,
        lon: Double
    ) : Notification(id, dateTime)

    class Message(
        id: Int,
        dateTime: String,
        title: String,
        body: String,
        image: String,
        attended: Boolean,
        sender: String,
        source: String,
        origin: String
    ) : Notification(id, dateTime)

    class Tracking(
        id: Int,
        dateTime: String,
        title: String,
        description: String,
        attended: Boolean
    ) : Notification(id, dateTime)

    class None(id: Int, dateTime: String) : Notification(id, dateTime)
}

enum class NotificationType {
    ALERT, MESSAGE, TRACKING
}

