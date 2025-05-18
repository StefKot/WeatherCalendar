package com.bas.weathercalendar.network.model

data class ObservationPut (
    val city: String,
    val observation_date: String,
    val observation_time: String,
    val temperature: Double,
    val precipitation_type: String
)