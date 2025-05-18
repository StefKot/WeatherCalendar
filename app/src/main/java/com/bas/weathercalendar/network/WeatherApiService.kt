package com.bas.weathercalendar.network

import com.bas.weathercalendar.network.model.ObservationGet
import com.bas.weathercalendar.network.model.ObservationPut
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface WeatherApiService {

    @POST("observations/")
    suspend fun createObservation(@Body observationData: ObservationPut): Response<ObservationGet> // OpenAPI возвращает WeatherObservation

    @GET("observations/")
    suspend fun getObservations(
        @Query("obs_date") obsDate: String? = null, // Формат YYYY-MM-DD
        @Query("start_date") startDate: String? = null, // Формат YYYY-MM-DD
        @Query("end_date") endDate: String? = null, // Формат YYYY-MM-DD
        @Query("city") city: String? = null
    ): Response<List<ObservationGet>> // OpenAPI возвращает массив WeatherObservation
}