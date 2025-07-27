package com.sudh.weatherforecast

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_home, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set current date
        val dateTextView = view.findViewById<TextView>(R.id.datetxt)
        val date = SimpleDateFormat("dd , MMMM", Locale.getDefault()).format(Date())
        dateTextView.text = date
    }

    override fun onResume() {
        super.onResume()
        fetchWeather()
    }

    private fun fetchWeather() {
        val prefs = requireContext().getSharedPreferences("weather_prefs", Activity.MODE_PRIVATE)
        val cityName = prefs.getString("city_name", "Delhi") ?: "Delhi"

        Toast.makeText(requireContext(), "City: $cityName", Toast.LENGTH_SHORT).show()

        val cityTextView = rootView.findViewById<TextView>(R.id.city)
        cityTextView.text = cityName

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(WeatherApiService::class.java)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val apiKey = BuildConfig.OPENWEATHER_API_KEY
                val geoList = api.getCoordinatesByCity(cityName, apiKey = apiKey)

                if (geoList.isNotEmpty()) {
                    val lat = geoList[0].lat
                    val lon = geoList[0].lon
                    Log.d("DEBUG", "Calling weather API with lat=$lat, lon=$lon")

                    val weather = api.getCurrentWeather(lat, lon, "metric", apiKey)

                    // Set values to UI
                    rootView.findViewById<TextView>(R.id.temperature).text = "${weather.main.temp}°C"
                    rootView.findViewById<TextView>(R.id.humidity).text = "${weather.main.humidity}%"
                    rootView.findViewById<TextView>(R.id.windspeed).text = "${weather.wind.speed}km/h"
                    rootView.findViewById<TextView>(R.id.feels_like_value).text = "${weather.main.feels_like}°C"
                    rootView.findViewById<TextView>(R.id.descriptiontxt).text = weather.weather[0].description

                    val iconCode = weather.weather[0].icon
                    val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
                    val iconImage = rootView.findViewById<ImageView>(R.id.topicon)

                    Glide.with(requireContext())
                        .load(iconUrl)
                        .into(iconImage)

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
