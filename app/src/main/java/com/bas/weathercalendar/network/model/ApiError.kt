package com.bas.weathercalendar.network.model

data class ValidationErrorDetail(
    val loc: List<String>, // "loc": ["body", "username"]
    val msg: String,       // "msg": "Value error, A user with this username already exists"
    val type: String       // "type": "value_error"
)

data class HttpValidationError(
    val detail: List<ValidationErrorDetail>
)