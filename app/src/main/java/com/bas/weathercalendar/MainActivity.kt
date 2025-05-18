package com.bas.weathercalendar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.bas.weathercalendar.data.TokenManager

// Предположим, у вас будут такие Activity:
import com.bas.weathercalendar.features.AddWeatherEntryActivity // Activity для добавления записи
import com.bas.weathercalendar.features.ViewWeatherEntriesActivity // Activity для просмотра записей

class MainActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tokenManager = TokenManager(applicationContext)

        if (tokenManager.getToken() == null) {
            // Токена нет, перенаправляем на экран входа
            navigateToAuthActivity()
            return // Предотвращаем выполнение остального кода onCreate, если пользователь не авторизован
        }

        setContentView(R.layout.activity_main)

        val addEntryButton: Button = findViewById(R.id.button_add_weather_entry)
        val viewEntriesButton: Button = findViewById(R.id.button_view_weather_entries)
        val logoutButton: Button = findViewById(R.id.button_exit)

        addEntryButton.setOnClickListener {
            // TODO: Заменить на реальный Intent для AddWeatherEntryActivity
             val intent = Intent(this, AddWeatherEntryActivity::class.java)
             startActivity(intent)
            // Для примера пока оставим Toast
//            android.widget.Toast.makeText(this, "Переход к добавлению записи", android.widget.Toast.LENGTH_SHORT).show()
        }

        viewEntriesButton.setOnClickListener {
            // TODO: Заменить на реальный Intent для ViewWeatherEntriesActivity
             val intent = Intent(this, ViewWeatherEntriesActivity::class.java)
             startActivity(intent)
            // Для примера пока оставим Toast
//            android.widget.Toast.makeText(this, "Переход к просмотру записей", android.widget.Toast.LENGTH_SHORT).show()
        }

        logoutButton.setOnClickListener {
            tokenManager.clearToken() // Очищаем токен
            navigateToAuthActivity()
        }
    }

    private fun navigateToAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Закрываем MainActivity
    }
}