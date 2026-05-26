package com.redes.app.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthTokenInterceptor(
    private val tokenProvider: TokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.header("Authorization") != null) {
            return chain.proceed(request)
        }

        val token = kotlinx.coroutines.runBlocking {
            tokenProvider.getIdToken()
        }

        val nextRequest = if (token.isNullOrBlank()) {
            request
        } else {
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(nextRequest)
    }
}
