package com.bas.weathercalendar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.bas.weathercalendar.data.TokenManager // Ваш TokenManager

class MainActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tokenManager = TokenManager(applicationContext)

        if (tokenManager.getToken() == null) {
            // Токена нет, перенаправляем на экран входа
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // Закрываем MainActivity, так как пользователь не авторизован
            return // Предотвращаем выполнение остального кода onCreate
        }

        setContentView(R.layout.activity_main)

        // Остальная логика для MainActivity
        val logoutButton: Button = findViewById(R.id.button_exit)
        logoutButton.setOnClickListener {
            tokenManager.clearToken() // Очищаем токен
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // Закрываем MainActivity после выхода
        }

    }
}