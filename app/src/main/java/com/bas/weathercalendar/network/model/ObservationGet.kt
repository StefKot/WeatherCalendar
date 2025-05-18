package com.bas.weathercalendar.network.model

class ObservationGet(
    val city: String,
    val observation_date: String,
    val observation_time: String,
    val temperature: Int,
    val precipitation_type: String,
    val id: Int,
    val user_id: Int
)