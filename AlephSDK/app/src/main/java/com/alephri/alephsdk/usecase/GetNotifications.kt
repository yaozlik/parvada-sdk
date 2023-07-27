package com.alephri.alephsdk.usecase

import android.util.Log
import com.alephri.alephsdk.base.AlephClient
import com.alephri.alephsdk.data.base.ErrorHandler
import com.alephri.alephsdk.data.base.State
import com.alephri.alephsdk.data.entity.Notification
import com.alephri.alephsdk.usecase.base.UseCase
import com.aleprhi.alephSDK.GetNotificationByIdQuery
import com.aleprhi.alephSDK.GetNotificationsQuery
import com.aleprhi.alephSDK.type.OrderEnum
import com.apollographql.apollo3.api.Optional
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart

class GetNotifications(private val token: String) :
    UseCase<List<Notification>, GetNotifications.Params>() {

    private val alephClient = AlephClient.getInstance().getGraphQLLoggedClient(token)

    data class Params(
        val limit: Int,
        val offset: Int
    )

    override suspend fun execute(params: Params): Flow<State<List<Notification>>> {
        val query = alephClient.query(
            GetNotificationsQuery(
                limit = Optional.present(params.limit),
                offset = Optional.present(params.offset),
                order = Optional.present(OrderEnum.DESC)
            )
        )

        return flow {
            if (token.isEmpty()) {
                emit(State.Failure(Exception(ErrorHandler.NO_LOGGED_MESSAGE)))
            }
            val response = query.execute()

            if (response.hasErrors()) {
                emit(State.Failure(Exception(ErrorHandler.UNKNOWN_ERROR)))
            } else {
                val notifications = mutableListOf<Notification>()
                val attendables = response.data?.attendables

                attendables?.forEach {
                    notifications.add(transform(it))
                }

                if (attendables == null) {
                    emit(State.Failure(Exception(ErrorHandler.NO_SUCH_DATA)))
                } else {
                    emit(State.Success(notifications))
                }
            }
        }.onStart {
            emit(State.Progress(true))
        }.catch {
            emit(State.Failure(it))
        }
    }

    private fun transform(attendable: GetNotificationsQuery.Attendable): Notification {
        val id = attendable.id

        val attended = attendable.attended

        val params = attendable.params

        val dateTime = params?.firstOrNull() {
            it.name == "date"
        }?.content ?: ""

        val body = params?.firstOrNull {
            it.name == "body"
        }?.content ?: ""

        val image = params?.firstOrNull {
            it.name == "image"
        }?.content ?: ""

        val sender = params?.firstOrNull {
            it.name == "sender"
        }?.content ?: ""

        val source = params?.firstOrNull {
            it.name == "source"
        }?.content ?: ""

        val title = params?.firstOrNull {
            it.name == "title"
        }?.content ?: ""

        val category = params?.firstOrNull {
            it.name == "category"
        }?.content ?: ""

        val location = params?.firstOrNull {
            it.name == "location"
        }?.content ?: ""

        val trackingTitle = params?.firstOrNull {
            it.name == "tracking_title"
        }?.content ?: ""

        val trackingDescription = params?.firstOrNull {
            it.name == "tracking_description"
        }?.content ?: ""

        val origin = params?.firstOrNull {
            it.name == "origin"
        }?.content ?: ""

        val latString = params?.firstOrNull {
            it.name == "lat"
        }?.content ?: "0.0"

        val lonString = params?.firstOrNull {
            it.name == "lon"
        }?.content ?: "0.0"

        val lat = latString.toDouble()

        val lon = lonString.toDouble()

        var notification: Notification = Notification.None(
            id = id,
            dateTime = dateTime
        )

        if (attendable.type == "external_communication") {
            if (category == "message") {
                notification = Notification.Message(
                    id = id,
                    dateTime = dateTime,
                    title = title,
                    body = body,
                    image = image,
                    attended = attended,
                    sender = sender,
                    source = source,
                    origin = origin
                )
            } else if (category == "alert"){
                notification = Notification.Alert(
                    id = id,
                    dateTime = dateTime,
                    title = title,
                    body = body,
                    image = image,
                    attended = attended,
                    sender = sender,
                    source = source,
                    origin = origin,
                    location = location,
                    lat = lat,
                    lon = lon
                )
            }
        } else if (attendable.type == "tracking") {
            notification = Notification.Tracking(
                id = id,
                dateTime = dateTime,
                title = trackingTitle,
                description = trackingDescription,
                attended = attended
            )
        }
        return notification
    }
}
