package com.bas.weathercalendar.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.bas.weathercalendar.network.RetrofitClient
import com.bas.weathercalendar.network.model.HttpValidationError
import com.bas.weathercalendar.network.model.ResetPassword
import com.google.gson.Gson
import kotlinx.coroutines.launch

sealed class ResetResult {
    object Loading : ResetResult()
    data class Success(val message: String) : ResetResult()
    data class Error(val errorMessage: String) : ResetResult()
}

class ResetPassViewModel(application: Application) : AndroidViewModel(application) {

    private val _resetStatus = MutableLiveData<ResetResult>()
    val resetStatus: LiveData<ResetResult> = _resetStatus

    fun resetPassword(ResetPassword: ResetPassword) {
        _resetStatus.value = ResetResult.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.createAuthService(context = application.applicationContext)
                .resetPassword(ResetPassword)
                if (response.isSuccessful) {
                    _resetStatus.postValue(ResetResult.Success("Пароль успешно изменён! Теперь вы можете войти."))
                } else {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        try {
                            val validationError = Gson().fromJson(errorBody, HttpValidationError::class.java)
                            val errorMessages = validationError.detail.joinToString(separator = "\n") { it.msg }
                            _resetStatus.postValue(ResetResult.Error("Ошибка смены пароля:\n$errorMessages"))
                        } catch (e: Exception) { // Если не удалось распарсить как HttpValidationError
                            _resetStatus.postValue(ResetResult.Error("Ошибка смены пароля: ${response.code()} - ${response.message()}"))
                        }
                    } else {
                        _resetStatus.postValue(ResetResult.Error("Ошибка смены пароля: ${response.code()}"))
                    }
                }
            } catch (e: Exception) {
                _resetStatus.postValue(ResetResult.Error("Ошибка сети: ${e.message}"))
            }
        }
    }
}