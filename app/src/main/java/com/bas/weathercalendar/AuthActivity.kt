package com.bas.weathercalendar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bas.weathercalendar.login.LoginFragment

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth) // Устанавливаем макет для AuthActivity

        // Загружаем LoginFragment в контейнер, если активность создается впервые
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_auth, LoginFragment())
                .commit()
        }
    }
}