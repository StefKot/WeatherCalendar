package com.bas.weathercalendar.network

import android.util.Log
import com.bas.weathercalendar.data.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .apply {
                tokenManager.getToken()?.let { token ->
                    Log.d("AuthInterceptor", "Token found: Bearer $token")
                    addHeader("Authorization", "Bearer $token")
                } ?: Log.w("AuthInterceptor", "Token is null, not adding Authorization header.")
            }
            .build()

        return chain.proceed(request)
    }
}