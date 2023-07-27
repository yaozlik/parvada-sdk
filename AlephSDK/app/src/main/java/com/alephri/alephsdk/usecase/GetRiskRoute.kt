package com.alephri.alephsdk.usecase

import com.alephri.alephsdk.base.AlephClient
import com.alephri.alephsdk.data.base.ErrorHandler
import com.alephri.alephsdk.data.base.State
import com.alephri.alephsdk.usecase.base.UseCase
import com.aleprhi.alephSDK.GetRiskRouterQuery
import com.aleprhi.alephSDK.type.GeoPointInput
import com.aleprhi.alephSDK.type.MobilityEnum
import com.apollographql.apollo3.api.Optional
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart

class GetRiskRoute(private val token: String) :
    UseCase<GetRiskRouterQuery.RiskRouter, GetRiskRoute.Params>() {

    private val alephClient = AlephClient.getInstance().getGraphQLLoggedClient(token)

    data class Params(
        val origin: GeoPointInput,
        val destination: GeoPointInput
    )

    override suspend fun execute(params: Params): Flow<State<GetRiskRouterQuery.RiskRouter>> {
        val query = alephClient.query(
            GetRiskRouterQuery(
                origin = params.origin,
                destination = params.destination,
                mobility = Optional.present(MobilityEnum.transit)
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
                val riskRoute = response.data?.riskRouter
                if (riskRoute == null) {
                    emit(State.Failure(Exception(ErrorHandler.NO_SUCH_DATA)))
                } else {
                    emit(State.Success(riskRoute))
                }
            }
        }.onStart {
            emit(State.Progress(true))
        }.catch {
            emit(State.Failure(it))
        }
    }
}
