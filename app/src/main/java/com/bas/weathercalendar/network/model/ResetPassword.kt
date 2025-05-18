package com.bas.weathercalendar.network.model

data class ResetPassword(
    val username: String,
    val new_password: String
)