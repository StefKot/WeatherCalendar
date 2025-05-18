package com.bas.weathercalendar // или com.bas.weathercalendar.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bas.weathercalendar.data.WeatherRepository
import com.bas.weathercalendar.network.RetrofitClient
import com.bas.weathercalendar.network.model.ObservationGet
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val weatherRepository: WeatherRepository
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // LiveData для списка наблюдений на выбранную дату
    private val _observations = MutableLiveData<List<ObservationGet>>()
    val observations: LiveData<List<ObservationGet>> = _observations

    // LiveData для выбранной даты (строка в формате YYYY-MM-DD)
    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> = _selectedDate

    // LiveData для состояния загрузки
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData для ошибок
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // LiveData для управления навигацией к AddWeatherEntryActivity
    private val _navigateToAddEntry = MutableLiveData<Pair<String, String?>?>() // Pair<Date, City?>
    val navigateToAddEntry: LiveData<Pair<String, String?>?> = _navigateToAddEntry


    init {
        // Инициализация репозитория
        val weatherApiService = RetrofitClient.createWeatherService(application)
        weatherRepository = WeatherRepository(weatherApiService)

        // Устанавливаем текущую дату при инициализации
        setSelectedDate(Calendar.getInstance())
    }

    fun setSelectedDate(calendar: Calendar) {
        val dateString = dateFormat.format(calendar.time)
        _selectedDate.value = dateString
        fetchObservationsForDate(dateString)
    }

    fun setSelectedDate(dateMillis: Long) {
        val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
        setSelectedDate(calendar)
    }

    fun fetchObservationsForDate(date: String, city: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = weatherRepository.getObservationsForDate(date, city)
                if (response.isSuccessful) {
                    _observations.value = response.body() ?: emptyList()
                } else {
                    _observations.value = emptyList() // Очищаем предыдущие данные
                    _error.value = "Ошибка загрузки данных: ${response.code()} ${response.message()}"
                }
            } catch (e: Exception) {
                _observations.value = emptyList()
                _error.value = "Ошибка сети: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Вызывается, когда нужно перейти к добавлению записи
    fun onAddEntryClicked() {
        // Мы передаем текущую выбранную дату, чтобы AddWeatherEntryActivity могла ее предзаполнить
        _navigateToAddEntry.value = Pair(_selectedDate.value ?: dateFormat.format(Calendar.getInstance().time), null) // Пока город не передаем
    }

    // Вызывается после завершения навигации
    fun onNavigationToAddEntryDone() {
        _navigateToAddEntry.value = null
    }

    // Для обновления данных после добавления новой записи
    fun refreshObservationsForSelectedDate() {
        _selectedDate.value?.let {
            fetchObservationsForDate(it)
        }
    }
}