package com.bas.weathercalendar

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bas.weathercalendar.data.TokenManager
import com.bas.weathercalendar.network.RetrofitClient
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.UnknownHostException

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var progressBar: ProgressBar

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("SplashActivity", "Coroutine Exception: ${throwable.localizedMessage}", throwable)
        runOnUiThread {
            progressBar.visibility = View.GONE
            Toast.makeText(
                this,
                "Ошибка сети: ${throwable.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
            navigateToAuth()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        tokenManager = TokenManager(applicationContext)
        progressBar = findViewById(R.id.splashProgressBar)

        val token = tokenManager.getToken()

        if (token != null) {
            progressBar.visibility = View.VISIBLE
            validateTokenWithStatusEndpoint() // Вызываем новый метод
        } else {
            navigateToAuth()
        }
    }

    private fun validateTokenWithStatusEndpoint() {
        lifecycleScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            try {
                // Вызываем новый эндпоинт /auth/status
                val response = RetrofitClient.instance.checkAuthStatus()

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body() != null) {
                        // Токен валиден, получен UserResponse
                        Log.i("SplashActivity", "Token is valid. User: ${response.body()?.username}")
                        navigateToMain()
                    } else if (response.code() == 401 || response.code() == 403) {
                        // Токен невалиден или истек (API должен вернуть 401 для неавторизованных запросов к защищенным эндпоинтам)
                        Log.w("SplashActivity", "Token validation failed with /auth/status: ${response.code()}")
                        tokenManager.clearToken()
                        navigateToAuth()
                    } else {
                        // Другая ошибка сервера
                        Log.e(
                            "SplashActivity",
                            "Server error during /auth/status validation: ${response.code()} - ${response.message()}"
                        )
                        Toast.makeText(
                            applicationContext,
                            "Ошибка сервера: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        navigateToAuth()
                    }
                }
            } catch (e: Exception) {
                Log.e(
                    "SplashActivity",
                    "Network or other error during /auth/status validation: ${e.localizedMessage}",
                    e
                )
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (e is ConnectException || e is UnknownHostException) {
                        Toast.makeText(
                            applicationContext,
                            "Не удалось подключиться к серверу. Проверьте интернет или адрес сервера.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Ошибка проверки сессии: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    navigateToAuth()
                }
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }
}