package com.alephri.alephsdk.usecase

import android.util.Log
import com.alephri.alephsdk.base.AlephClient
import com.alephri.alephsdk.data.base.State
import com.alephri.alephsdk.data.request.LocationRequest
import com.alephri.alephsdk.usecase.base.UseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SendReading : UseCase<Boolean, SendReading.Params>() {
    private val apiService = AlephClient.getInstance().getApiClient()

    data class Params(
        val header: Map<String, String>,
        val request: LocationRequest
    )

    override suspend fun execute(params: Params): Flow<State<Boolean>> {
        return flow {
            apiService.submitLocation(
                params.header,
                params.request
            ).run {
                val bodyResponse = body()
                if (isSuccessful && bodyResponse != null) {
                    Log.d("-", bodyResponse.string())
                    val response = true
                    emit(State.Success(response))
                } else {
                    Log.d("-", "failed")
                    emit(State.Failure(Exception("Failed")))
                }
            }
        }
    }
}
