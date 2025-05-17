package com.bas.weathercalendar.network

import android.content.Context
import android.util.Log // Импорт для логгирования
import com.bas.weathercalendar.data.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context: Context) : Interceptor {
    private val tokenManager = TokenManager(context.applicationContext) // Убедитесь, что context правильный

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
        val token = tokenManager.getToken() // Получаем токен

        if (token != null) {
            Log.d("AuthInterceptor", "Token found: Bearer $token") // Логируем токен
            requestBuilder.addHeader("Authorization", "Bearer $token")
        } else {
            Log.w("AuthInterceptor", "Token is null, not adding Authorization header.")
        }

        // Важно: убедитесь, что вы всегда возвращаете chain.proceed()
        return chain.proceed(requestBuilder.build())
    }
}