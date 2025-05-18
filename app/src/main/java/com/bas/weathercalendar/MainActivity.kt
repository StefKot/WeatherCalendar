package com.bas.weathercalendar

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels // Для by viewModels()
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bas.weathercalendar.data.TokenManager
import com.bas.weathercalendar.features.AddWeatherEntryActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private val viewModel: MainActivityViewModel by viewModels() // Инициализация ViewModel

    private lateinit var calendarView: CalendarView
    private lateinit var recyclerViewObservations: RecyclerView
    private lateinit var observationAdapter: WeatherObservationAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewNoData: TextView
    private lateinit var fabAddEntry: FloatingActionButton
    private lateinit var buttonExit: Button

    private val currentCalendar = Calendar.getInstance() // Для управления отображаемым месяцем/годом

    // ActivityResultLauncher для AddWeatherEntryActivity
    private val addWeatherEntryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Запись была успешно добавлена, обновляем список наблюдений
            viewModel.refreshObservationsForSelectedDate()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tokenManager = TokenManager(applicationContext)

        if (tokenManager.getToken() == null) {
            navigateToAuthActivity()
            return
        }

        setContentView(R.layout.activity_main)

        // Инициализация View
        calendarView = findViewById(R.id.calendar_view)
        recyclerViewObservations = findViewById(R.id.recycler_view_observations)
        progressBar = findViewById(R.id.progress_bar_main)
        textViewNoData = findViewById(R.id.text_view_no_data)
        fabAddEntry = findViewById(R.id.button_add_weather_entry_main)
        buttonExit = findViewById(R.id.button_exit)


        setupRecyclerView()
        setupCalendarView()
        setupObservers()
        setupButtonClickListeners()

    }


    private fun setupRecyclerView() {
        observationAdapter = WeatherObservationAdapter() // Создали адаптер ранее
        recyclerViewObservations.apply {
            adapter = observationAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun setupCalendarView() {
        // Устанавливаем слушатель выбора даты
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            viewModel.setSelectedDate(selectedCalendar) // ViewModel загрузит данные
            // Обновляем currentCalendar, чтобы кнопки навигации по месяцам работали корректно
            currentCalendar.set(year, month, dayOfMonth)
        }
        // Устанавливаем текущую дату в CalendarView из ViewModel или текущую системную
        viewModel.selectedDate.value?.let { dateStr ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            try {
                calendarView.date = sdf.parse(dateStr)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                calendarView.date = System.currentTimeMillis()
            }
        } ?: run {
            calendarView.date = System.currentTimeMillis()
        }
    }

    private fun setupObservers() {
        viewModel.observations.observe(this) { observations ->
            observationAdapter.submitList(observations)
            textViewNoData.visibility = if (observations.isNullOrEmpty() && !viewModel.isLoading.value!!) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) { // Если загрузка, скрываем текст "нет данных"
                textViewNoData.visibility = View.GONE
            } else { // Если загрузка закончилась, проверяем, есть ли данные
                textViewNoData.visibility = if (viewModel.observations.value.isNullOrEmpty()) View.VISIBLE else View.GONE
            }
        }

        viewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.selectedDate.observe(this) { date ->

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            try {
                val parsedDate = sdf.parse(date)
                if (parsedDate != null) {
                    currentCalendar.time = parsedDate
                }
            } catch (e: Exception) {
                // Handle parsing error
            }
        }

        viewModel.navigateToAddEntry.observe(this) { dateAndCityPair ->
            dateAndCityPair?.let { (date, city) ->
                val intent = Intent(this, AddWeatherEntryActivity::class.java).apply {
                    putExtra(AddWeatherEntryActivity.EXTRA_DATE, date) // Передаем выбранную дату
                }
                addWeatherEntryLauncher.launch(intent) // Запускаем для получения результата
                viewModel.onNavigationToAddEntryDone() // Сбрасываем событие навигации
            }
        }
    }

    private fun setupButtonClickListeners() {
        fabAddEntry.setOnClickListener {
            viewModel.onAddEntryClicked()
        }

        buttonExit.setOnClickListener {
            tokenManager.clearToken()
            navigateToAuthActivity()
        }

    }

    private fun navigateToAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}