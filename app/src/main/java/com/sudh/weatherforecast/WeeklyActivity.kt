package com.sudh.weatherforecast

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

class WeeklyActivity : AppCompatActivity() {

    private val apiKey = BuildConfig.OPENWEATHER_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly)

        val cityName = intent.getStringExtra("city_name")
        findViewById<TextView>(R.id.city).text = cityName ?: "Delhi"

        lifecycleScope.launch {
            try {
                val city = cityName ?: "Delhi"
                val api = RetrofitClient.geoApi

                val geoList = api.getCoordinatesByCity(city, apiKey = apiKey)

                if (geoList.isNotEmpty()) {
                    val lat = geoList[0].lat
                    val lon = geoList[0].lon

                    val forecast = api.get7DayForecast(lat, lon, apiKey, 9, "metric")
                    val tomorrowForecast = forecast.list[1]

                    findViewById<TextView>(R.id.temperature).text = "${tomorrowForecast.temp.day}°C"
                    findViewById<TextView>(R.id.humidity).text = "${tomorrowForecast.humidity}%"
                    findViewById<TextView>(R.id.windspeed).text = "${tomorrowForecast.speed} km/h"
                    findViewById<TextView>(R.id.feels_like_value).text = "${tomorrowForecast.feels_like.day}°C"

                    val desc = tomorrowForecast.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "--"
                    findViewById<TextView>(R.id.descriptiontxt).text = desc

                    val iconCode = tomorrowForecast.weather.firstOrNull()?.icon
                    val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
                    Glide.with(this@WeeklyActivity).load(iconUrl).into(findViewById(R.id.topicon))

                    val recyclerView = findViewById<RecyclerView>(R.id.weeklyRecyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(this@WeeklyActivity)
                    val filteredList = forecast.list.subList(2, forecast.list.size)
                    recyclerView.adapter = WeeklyAdapter(filteredList)

                    val response = RetrofitClient.geoApi.getAirQuality(lat, lon, apiKey)
                    val components = response.list.first().components

                    val aqi = calculateNAQI(components.pm2_5, components.pm10)

                    // Show NAQI
                    findViewById<TextView>(R.id.aqiText).text = "$aqi"
                } else {
                    Log.e("DEBUG", "geoList is empty")
                    Toast.makeText(this@WeeklyActivity, "City not found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("DEBUG", "API error: ${e.message}")
            }
        }

        findViewById<ImageButton>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<ImageButton>(R.id.navSearch).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        findViewById<ImageButton>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}