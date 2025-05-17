package com.bas.weathercalendar.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class TokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val USER_TOKEN = "user_token"
    }

    fun saveToken(token: String) {
        prefs.edit() {
            putString(USER_TOKEN, token)
        }
    }

    fun getToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun clearToken() {
        prefs.edit() {
            remove(USER_TOKEN)
        }
    }
}