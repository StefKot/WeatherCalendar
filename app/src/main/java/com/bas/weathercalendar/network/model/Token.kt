package com.bas.weathercalendar.network.model

import com.google.gson.annotations.SerializedName

data class Token(
    @SerializedName("access_token") // OpenAPI использует snake_case
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String
)