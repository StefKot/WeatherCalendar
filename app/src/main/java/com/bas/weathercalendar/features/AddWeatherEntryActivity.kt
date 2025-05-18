package com.bas.weathercalendar.features // Укажите ваш пакет

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bas.weathercalendar.AuthActivity
import com.bas.weathercalendar.MainActivity
import com.bas.weathercalendar.R
import com.bas.weathercalendar.data.TokenManager
import com.bas.weathercalendar.network.RetrofitClient
import com.bas.weathercalendar.network.model.ObservationPut
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddWeatherEntryActivity : AppCompatActivity() {

    private lateinit var textInputLayoutCity: TextInputLayout
    private lateinit var editTextCity: TextInputEditText
    private lateinit var buttonSelectDate: Button
    private lateinit var textViewSelectedDate: TextView
    private lateinit var buttonSelectTime: Button
    private lateinit var textViewSelectedTime: TextView
    private lateinit var textInputLayoutTemperature: TextInputLayout
    private lateinit var editTextTemperature: TextInputEditText
    private lateinit var spinnerPrecipitation: Spinner
    private lateinit var buttonSaveEntry: Button
    private lateinit var tokenManager: TokenManager
    private lateinit var buttonBack: Button

    private val calendar: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("SplashActivity", "Coroutine Exception: ${throwable.localizedMessage}", throwable)
        runOnUiThread {
            Toast.makeText(
                this,
                "Ошибка сети: ${throwable.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
            navigateToAuth()
        }
    }

    companion object {
        const val EXTRA_DATE = "com.bas.weathercalendar.features.addentry.EXTRA_DATE"
        // const val EXTRA_CITY = "com.bas.weathercalendar.features.addentry.EXTRA_CITY" // Если будете передавать город
    }

    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        setResult(RESULT_OK)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_weather_entry)

        // Инициализация View
        textInputLayoutCity = findViewById(R.id.text_input_layout_city)
        editTextCity = findViewById(R.id.edit_text_city)
        buttonSelectDate = findViewById(R.id.button_select_date)
        textViewSelectedDate = findViewById(R.id.text_view_selected_date)
        buttonSelectTime = findViewById(R.id.button_select_time)
        textViewSelectedTime = findViewById(R.id.text_view_selected_time)
        textInputLayoutTemperature = findViewById(R.id.text_input_layout_temperature)
        editTextTemperature = findViewById(R.id.edit_text_temperature)
        spinnerPrecipitation = findViewById(R.id.spinner_precipitation)
        buttonSaveEntry = findViewById(R.id.button_save_entry)
        buttonBack = findViewById(R.id.button_back)

        setupDateTimePicker()
        setupPrecipitationSpinner()
        setupSaveButton()

        // Устанавливаем текущие дату и время по умолчанию
        updateSelectedDateText()
        updateSelectedTimeText()

        val preselectedDateString = intent.getStringExtra(EXTRA_DATE)
        if (preselectedDateString != null) {
            try {
                val parsedDate = dateFormat.parse(preselectedDateString) // dateFormat из вашего класса
                if (parsedDate != null) {
                    calendar.time = parsedDate // calendar из вашего класса
                }
            } catch (e: Exception) {
                // Ошибка парсинга, используем текущую дату
                Log.e("AddWeatherEntryActivity", "Error parsing date: ${e.message}", e)
                calendar.time = Date()
            }
        }

        updateSelectedDateText()
        updateSelectedTimeText()
    }

        private fun setupDateTimePicker() {
        buttonSelectDate.setOnClickListener {
            showDatePicker()
        }

        buttonSelectTime.setOnClickListener {
            showTimePicker()
        }
        buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(Calendar.YEAR, selectedYear)
            calendar.set(Calendar.MONTH, selectedMonth)
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
            updateSelectedDateText()
        }, year, month, day).show()
    }

    private fun updateSelectedDateText() {
        textViewSelectedDate.text = dateFormat.format(calendar.time)
    }

    private fun showTimePicker() {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            calendar.set(Calendar.MINUTE, selectedMinute)
            updateSelectedTimeText()
        }, hour, minute, true).show() // true для 24-часового формата
    }

    private fun updateSelectedTimeText() {
        textViewSelectedTime.text = timeFormat.format(calendar.time)
    }

    private fun setupPrecipitationSpinner() {
        ArrayAdapter.createFromResource(
            this,
            R.array.precipitation_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerPrecipitation.adapter = adapter
        }
    }

    private fun setupSaveButton() {
        buttonSaveEntry.setOnClickListener {
            if (validateInput()) {
                saveWeatherEntry()
            }
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true

        if (editTextCity.text.toString().trim().isEmpty()) {
            textInputLayoutCity.error = getString(R.string.error_field_required)
            isValid = false
        } else {
            textInputLayoutCity.error = null
        }

        if (editTextTemperature.text.toString().trim().isEmpty()) {
            textInputLayoutTemperature.error = getString(R.string.error_field_required)
            isValid = false
        } else {
            textInputLayoutTemperature.error = null
        }


        return isValid
    }

    private fun saveWeatherEntry() {
        val city = editTextCity.text.toString().trim()
        val date = textViewSelectedDate.text.toString() // Уже в формате "yyyy-MM-dd"
        val time = textViewSelectedTime.text.toString() // Уже в формате "HH:mm"
        val temperatureString = editTextTemperature.text.toString().trim()
        val precipitation = spinnerPrecipitation.selectedItem.toString()

        // Проверка, что температура не пустая (хотя validateInput это уже делает)
        if (temperatureString.isEmpty()) {
            Toast.makeText(this, "Температура не может быть пустой", Toast.LENGTH_SHORT).show()
            return
        }
        val temperature = temperatureString.toDoubleOrNull()

        if (temperature == null) {
            textInputLayoutTemperature.error = "Некорректное значение температуры"
            Toast.makeText(this, "Некорректное значение температуры", Toast.LENGTH_SHORT).show()
            return
        } else {
            textInputLayoutTemperature.error = null
        }

        val observation = ObservationPut(city = city, observation_date = date, observation_time = time, temperature = temperature, precipitation_type = precipitation)

        lifecycleScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val checkToken = RetrofitClient.createAuthService(context = applicationContext).checkAuthStatus()
            if (!checkToken.isSuccessful) {
                Log.w("AddWeatherEntryActivity", "Token validation failed with /auth/status: ${checkToken.code()}")
                Toast.makeText(
                    applicationContext,
                    "Ошибка сервера: ${checkToken.code()}",
                    Toast.LENGTH_SHORT
                ).show()
                tokenManager.clearToken()
                navigateToAuth()
            }
            val response = RetrofitClient.createWeatherService(context = applicationContext).createObservation(observation)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Успешно сохранено",
                        Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Ошибка сервера: ${response.code()}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
        finish()
    }
}