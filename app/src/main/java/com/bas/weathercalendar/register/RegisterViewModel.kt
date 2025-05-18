package com.bas.weathercalendar.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.bas.weathercalendar.network.RetrofitClient
import com.bas.weathercalendar.network.model.HttpValidationError
import com.bas.weathercalendar.network.model.UserCreate
import com.google.gson.Gson
import kotlinx.coroutines.launch

sealed class RegistrationResult {
    object Loading : RegistrationResult()
    data class Success(val message: String) : RegistrationResult()
    data class Error(val errorMessage: String) : RegistrationResult()
}

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val _registrationStatus = MutableLiveData<RegistrationResult>()
    val registrationStatus: LiveData<RegistrationResult> = _registrationStatus

    fun registerUser(userCreate: UserCreate) {
        _registrationStatus.value = RegistrationResult.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.createAuthService(context = application.applicationContext)
                    .registerUser(userCreate)
                if (response.isSuccessful) {
                    // OpenAPI говорит, что при успехе (201) возвращается UserCreate
                    _registrationStatus.postValue(RegistrationResult.Success("Пользователь успешно зарегистрирован! Теперь вы можете войти."))
                } else {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        try {
                            val validationError = Gson().fromJson(errorBody, HttpValidationError::class.java)
                            val errorMessages = validationError.detail.joinToString(separator = "\n") { it.msg }
                            _registrationStatus.postValue(RegistrationResult.Error("Ошибка регистрации:\n$errorMessages"))
                        } catch (e: Exception) { // Если не удалось распарсить как HttpValidationError
                            _registrationStatus.postValue(RegistrationResult.Error("Ошибка регистрации: ${response.code()} - ${response.message()}"))
                        }
                    } else {
                        _registrationStatus.postValue(RegistrationResult.Error("Ошибка регистрации: ${response.code()}"))
                    }
                }
            } catch (e: Exception) {
                _registrationStatus.postValue(RegistrationResult.Error("Ошибка сети: ${e.message}"))
            }
        }
    }
}