package com.bas.weathercalendar // или com.bas.weathercalendar.ui.main или ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bas.weathercalendar.R
import com.bas.weathercalendar.network.model.ObservationGet // Ваша модель
import java.time.format.DateTimeFormatter

class WeatherObservationAdapter : ListAdapter<ObservationGet, WeatherObservationAdapter.ViewHolder>(ObservationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weather_observation, parent, false) // Создадим этот layout ниже
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTime: TextView = itemView.findViewById(R.id.text_view_item_time)
        private val textViewCity: TextView = itemView.findViewById(R.id.text_view_item_city)
        private val textViewTemperature: TextView = itemView.findViewById(R.id.text_view_item_temperature)
        private val textViewPrecipitation: TextView = itemView.findViewById(R.id.text_view_item_precipitation)

        fun bind(observation: ObservationGet) {
            textViewTime.text = "Время: ${observation.observation_time}"
            textViewCity.text = "Город: ${observation.city}"
            textViewTemperature.text = "Температура: ${observation.temperature}°C"
            textViewPrecipitation.text = "Осадки: ${observation.precipitation_type}"
        }
    }

    class ObservationDiffCallback : DiffUtil.ItemCallback<ObservationGet>() {
        override fun areItemsTheSame(oldItem: ObservationGet, newItem: ObservationGet): Boolean {
            return oldItem.id == newItem.id // Предполагаем, что у ObservationGet есть id
        }

        override fun areContentsTheSame(oldItem: ObservationGet, newItem: ObservationGet): Boolean {
            return oldItem.observation_time == newItem.observation_time &&
                    oldItem.city == newItem.city &&
                    oldItem.temperature == newItem.temperature &&
                    oldItem.precipitation_type == newItem.precipitation_type
        }
    }
}