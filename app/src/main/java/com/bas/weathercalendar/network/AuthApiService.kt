package com.bas.weathercalendar.network

import com.bas.weathercalendar.network.model.Token
import com.bas.weathercalendar.network.model.UserCreate
import com.bas.weathercalendar.network.model.UserLogin
import com.bas.weathercalendar.network.model.ObservationPut
import com.bas.weathercalendar.network.model.ObservationGet
import com.bas.weathercalendar.network.model.ResetPassword
import com.bas.weathercalendar.network.model.UserResponse
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthApiService {

    @POST("auth/register") // Путь из OpenAPI
    suspend fun registerUser(@Body userCreate: UserCreate): Response<UserCreate> // В ответе ожидаем UserCreate

    @POST("auth/login") // Путь из OpenAPI
    suspend fun loginUser(@Body userLogin: UserLogin): Response<Token>

    @GET("auth/status")
    suspend fun checkAuthStatus(): Response<UserResponse> // Ожидаем UserResponse при успехе (200)

    @PUT("auth/reset_password")
    suspend fun resetPassword(@Body ResetPassword: ResetPassword): Response<JsonObject> // В ответе ожидаем ResetPassword
}