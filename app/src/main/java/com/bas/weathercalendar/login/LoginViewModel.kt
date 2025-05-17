package com.bas.weathercalendar.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bas.weathercalendar.data.TokenManager
import com.bas.weathercalendar.network.RetrofitClient
import com.bas.weathercalendar.network.model.HttpValidationError
import com.bas.weathercalendar.network.model.UserLogin
import com.google.gson.Gson
import kotlinx.coroutines.launch

sealed class LoginResult {
    object Loading : LoginResult()
    object Success : LoginResult() // Просто успех, токен сохраним внутри
    data class Error(val errorMessage: String) : LoginResult()
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _loginStatus = MutableLiveData<LoginResult>()
    val loginStatus: LiveData<LoginResult> = _loginStatus

    private val tokenManager = TokenManager(application.applicationContext)

    fun loginUser(userLogin: UserLogin) {
        _loginStatus.value = LoginResult.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.loginUser(userLogin)
                if (response.isSuccessful && response.body() != null) {
                    tokenManager.saveToken(response.body()!!.accessToken)
                    _loginStatus.postValue(LoginResult.Success)
                } else {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        try {
                            // Попробуем распарсить ошибку как HTTPValidationError (код 422)
                            val validationError = Gson().fromJson(errorBody, HttpValidationError::class.java)
                            val errorMessages = validationError.detail.joinToString(separator = "\n") { it.msg }
                            _loginStatus.postValue(LoginResult.Error("Ошибка входа:\n$errorMessages"))
                        } catch (e: Exception) {
                            // Если не 422 или другая структура ошибки (например, 401 Unauthorized)
                            _loginStatus.postValue(LoginResult.Error("Ошибка входа: ${response.code()} - Неверный логин или пароль."))
                        }
                    } else {
                        _loginStatus.postValue(LoginResult.Error("Ошибка входа: ${response.code()} - Неверный логин или пароль."))
                    }
                }
            } catch (e: Exception) {
                _loginStatus.postValue(LoginResult.Error("Ошибка сети: ${e.message}"))
            }
        }
    }
}