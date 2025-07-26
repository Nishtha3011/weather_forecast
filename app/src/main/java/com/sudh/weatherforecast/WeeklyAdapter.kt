package com.sudh.weatherforecast

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sudh.weatherforecast.models.ForecastDay

class WeeklyAdapter(private val forecastList: List<ForecastDay>) :
    RecyclerView.Adapter<WeeklyAdapter.WeeklyViewHolder>() {

    class WeeklyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayText: TextView = itemView.findViewById(R.id.dayName)
        val tempText: TextView = itemView.findViewById(R.id.dayTemp)
        val iconImage: ImageView = itemView.findViewById(R.id.dayIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeeklyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast, parent, false)
        return WeeklyViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeeklyViewHolder, position: Int) {
        val daily = forecastList[position]

        // Set abbreviated day (e.g. MON, TUE, ...)
        val dayName = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
            .format(java.util.Date(daily.dt * 1000L))
            .uppercase() // Converts to MON, TUE, etc.
        holder.dayText.text = dayName


        holder.tempText.text = "${daily.temp.max.toInt()}°/${daily.temp.min.toInt()}°"

        // Set icon
        val iconCode = daily.weather[0].icon
        val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
        Glide.with(holder.itemView.context).load(iconUrl).into(holder.iconImage)
    }

    override fun getItemCount(): Int = forecastList.size
}
