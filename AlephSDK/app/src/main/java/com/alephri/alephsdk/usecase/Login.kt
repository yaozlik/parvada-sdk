package com.alephri.alephsdk.usecase

import com.alephri.alephsdk.base.AlephClient
import com.alephri.alephsdk.data.base.ErrorHandler
import com.alephri.alephsdk.data.base.State
import com.alephri.alephsdk.usecase.base.UseCase
import com.aleprhi.alephSDK.LoginMutation
import com.apollographql.apollo3.api.Optional
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart

class Login : UseCase<String, Login.Params>() {
    private val alephClient = AlephClient.getInstance().getGraphQLClient()

    data class Params(
        val email: String,
        val password: String,
        val deviceId: String
    )

    override suspend fun execute(params: Params): Flow<State<String>> {
        val mutation = alephClient.mutation(
            LoginMutation(
                email = params.email,
                password = params.password,
                deviceId = Optional.present(params.deviceId)
            )
        )

        return flow {
            val response = mutation.execute()
            if (response.hasErrors()) {
                emit(State.Failure(Exception(ErrorHandler.LOGIN_ERROR)))
            } else {
                val token: String = response.data?.login?.token ?: ""
                emit(State.Success(token))
            }
        }.onStart {
            emit(State.Progress(true))
        }.catch {
            emit(State.Failure(it))
        }
    }
}
