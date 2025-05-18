package com.bas.weathercalendar.network

import com.bas.weathercalendar.network.model.Token
import com.bas.weathercalendar.network.model.UserCreate
import com.bas.weathercalendar.network.model.UserLogin
import com.bas.weathercalendar.network.model.Observation
import com.bas.weathercalendar.network.model.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/register") // Путь из OpenAPI
    suspend fun registerUser(@Body userCreate: UserCreate): Response<UserCreate> // В ответе ожидаем UserCreate

    @POST("auth/login") // Путь из OpenAPI
    suspend fun loginUser(@Body userLogin: UserLogin): Response<Token>

    @GET("auth/status")
    suspend fun checkAuthStatus(): Response<UserResponse> // Ожидаем UserResponse при успехе (200)

    @GET("/observations/") // Путь из OpenAPI
    suspend fun readObservations(): Response<List<Observation>>

    @POST("/reset_password/")
    suspend fun resetPassword(@Body userCreate: UserCreate): Response<UserCreate> // В ответе ожидаем UserCreate
}