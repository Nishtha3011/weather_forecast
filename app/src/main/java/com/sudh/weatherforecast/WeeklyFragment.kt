package com.sudh.weatherforecast

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class WeeklyFragment : Fragment() {

    private val apiKey = BuildConfig.OPENWEATHER_API_KEY
    private var currentCityName: String? = null
    private var rootView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_weekly, container, false)
        return rootView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentCityName = getCityFromPrefs()
        loadWeeklyWeather(currentCityName!!)
    }

    override fun onResume() {
        super.onResume()
        val latestCity = getCityFromPrefs()
        if (latestCity != currentCityName) {
            currentCityName = latestCity
            loadWeeklyWeather(currentCityName!!)
        }
    }

    private fun getCityFromPrefs(): String {
        return requireContext()
            .getSharedPreferences("weather_prefs", Activity.MODE_PRIVATE)
            .getString("city_name", "Delhi") ?: "Delhi"
    }

    private fun loadWeeklyWeather(cityName: String) {
        rootView?.findViewById<TextView>(R.id.city)?.text = cityName

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = RetrofitClient.geoApi
                val geoList = api.getCoordinatesByCity(cityName, apiKey = apiKey)

                if (geoList.isNotEmpty()) {
                    val lat = geoList[0].lat
                    val lon = geoList[0].lon

                    // Get 7-day forecast
                    val forecast = api.get7DayForecast(lat, lon, apiKey, 9, "metric")
                    val tomorrowForecast = forecast.list[1]

                    rootView?.findViewById<TextView>(R.id.temperature)?.text = "${tomorrowForecast.temp.day}°C"
                    rootView?.findViewById<TextView>(R.id.humidity)?.text = "${tomorrowForecast.humidity}%"
                    rootView?.findViewById<TextView>(R.id.windspeed)?.text = "${tomorrowForecast.speed} km/h"
                    rootView?.findViewById<TextView>(R.id.feels_like_value)?.text = "${tomorrowForecast.feels_like.day}°C"

                    val desc = tomorrowForecast.weather.firstOrNull()?.description
                        ?.replaceFirstChar { it.uppercase() } ?: "--"
                    rootView?.findViewById<TextView>(R.id.descriptiontxt)?.text = desc

                    val iconCode = tomorrowForecast.weather.firstOrNull()?.icon
                    val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
                    Glide.with(requireContext()).load(iconUrl).into(rootView?.findViewById(R.id.topicon)!!)

                    // Setup RecyclerView for 5-day forecast
                    val recyclerView = rootView?.findViewById<RecyclerView>(R.id.weeklyRecyclerView)
                    recyclerView?.layoutManager = LinearLayoutManager(requireContext())
                    recyclerView?.adapter = WeeklyAdapter(forecast.list.subList(2, forecast.list.size))

                    // Get AQI
                    val response = api.getAirQuality(lat, lon, apiKey)
                    val components = response.list.first().components
                    val aqi = calculateNAQI(components.pm2_5, components.pm10)
                    rootView?.findViewById<TextView>(R.id.aqiText)?.text = "$aqi"
                } else {
                    Log.e("DEBUG", "geoList is empty")
                    Toast.makeText(requireContext(), "City not found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("DEBUG", "API error: ${e.message}")
            }
        }
    }
}
