package com.bas.weathercalendar.network.model

data class Observation (
    val id: Int,
    val userId: Int,
    val city: String,
    val obsDate: String,
    val obsTime: String,
    val temperature: Double,
    val precipitationType: String
)