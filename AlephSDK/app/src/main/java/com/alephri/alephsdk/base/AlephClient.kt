package com.alephri.alephsdk.base

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AlephClient private constructor() {

    companion object {
        const val graphQlEndpoint = "https://alephri-parvada-prod.herokuapp.com/api/graphql"

        @Volatile
        private var instance: AlephClient? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: AlephClient().also { instance = it }
            }
    }

    fun getGraphQLClient(): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl(graphQlEndpoint)
            .build()
    }

    fun getGraphQLLoggedClient(token: String): ApolloClient {
        val interceptor = AuthorizationInterceptor(token)
        return ApolloClient.Builder()
            .serverUrl(graphQlEndpoint)
            .addHttpInterceptor(interceptor)
            .build()
    }

    fun getApiClient(): ApiService {
        val httpClient = OkHttpClient.Builder().apply {
            readTimeout(120, TimeUnit.SECONDS)
            connectTimeout(120, TimeUnit.SECONDS)
        }.build()

        val client = Retrofit.Builder()
            .baseUrl("https://hnckdcglr8.execute-api.us-west-2.amazonaws.com/prod/")
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().serializeNulls().create()))
            .client(httpClient)
            .build()

        return client.create(ApiService::class.java)
    }
}

class AuthorizationInterceptor(private val token: String) : HttpInterceptor {

    override suspend fun intercept(
        request: HttpRequest,
        chain: HttpInterceptorChain
    ): HttpResponse {
        return chain.proceed(
            request.newBuilder().addHeader("Authorization", "Bearer $token").build()
        )
    }
}
