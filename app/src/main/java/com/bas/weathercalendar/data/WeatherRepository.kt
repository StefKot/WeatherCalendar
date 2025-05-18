package com.bas.weathercalendar.data

import com.bas.weathercalendar.network.WeatherApiService
import com.bas.weathercalendar.network.model.ObservationGet
import com.bas.weathercalendar.network.model.ObservationPut
import retrofit2.Response

class WeatherRepository(private val weatherApiService: WeatherApiService) {

    suspend fun getObservationsForDate(date: String, city: String? = null): Response<List<ObservationGet>> {
        return weatherApiService.getObservations(obsDate = date, city = city)
    }

    suspend fun createObservation(observationData: ObservationPut): Response<ObservationGet> {
        return weatherApiService.createObservation(observationData)
    }

    suspend fun getObservationsForPeriod(startDate: String, endDate: String, city: String? = null): Response<List<ObservationGet>> {
        return weatherApiService.getObservations(startDate = startDate, endDate = endDate, city = city)
    }
}