package com.bas.weathercalendar.network

import android.content.Context // Нужен для инициализации TokenManager в Interceptor
import com.bas.weathercalendar.data.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://95.163.152.133:8000/"

    fun createAuthService(context: Context): AuthApiService {
        val tokenManager = TokenManager(context.applicationContext)
        val authInterceptor = AuthInterceptor(tokenManager)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Для отладки, можно убрать в релизе
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create()) // Убедитесь, что конвертер соответствует вашему API
            .build()
            .create(AuthApiService::class.java)
    }

    fun createWeatherService(context: Context): WeatherApiService {
        val tokenManager = TokenManager(context.applicationContext)
        val authInterceptor = AuthInterceptor(tokenManager) // Используем тот же интерцептор для авторизации

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // Важно для эндпоинтов, требующих авторизации
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}