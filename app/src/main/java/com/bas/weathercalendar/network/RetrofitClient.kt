package com.bas.weathercalendar.network

import android.content.Context // Нужен для инициализации TokenManager в Interceptor
import com.bas.weathercalendar.App
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://95.163.152.133:8000/"

    // Ленивая инициализация Context из Application класса
    // Это нужно, чтобы AuthInterceptor мог получить доступ к TokenManager, который требует Context
    private val applicationContext: Context by lazy {
        App.instance.applicationContext
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = AuthInterceptor(applicationContext) // Передаем контекст

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor) // Добавляем наш AuthInterceptor
        .build()

    val instance: AuthApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Используем OkHttpClient с AuthInterceptor
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(AuthApiService::class.java)
    }
}