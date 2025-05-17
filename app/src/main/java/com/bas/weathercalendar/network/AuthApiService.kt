package com.bas.weathercalendar.network

import com.bas.weathercalendar.network.model.Token
import com.bas.weathercalendar.network.model.UserCreate
import com.bas.weathercalendar.network.model.UserLogin
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/register") // Путь из OpenAPI
    suspend fun registerUser(@Body userCreate: UserCreate): Response<UserCreate> // В ответе ожидаем UserCreate (согласно OpenAPI)

    @POST("auth/login") // Путь из OpenAPI
    suspend fun loginUser(@Body userLogin: UserLogin): Response<Token>
}