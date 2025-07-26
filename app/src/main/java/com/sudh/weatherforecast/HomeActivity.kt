package com.sudh.weatherforecast

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class HomeActivity : AppCompatActivity() {

    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)



        // Get city name
        val cityName = intent.getStringExtra("city_name")
        Toast.makeText(this, "City: $cityName", Toast.LENGTH_LONG).show()
        val cityTextView = findViewById<TextView>(R.id.city)
        cityTextView.text = cityName ?: "Delhi"


        // Retrofit setup
        val retrofit=Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        val api = retrofit.create(WeatherApiService::class.java)

        // Network call
        // Coroutine for network call
        lifecycleScope.launch {
            try {
                val apiKey = BuildConfig.OPENWEATHER_API_KEY

                val geoList = api.getCoordinatesByCity(cityName ?: "Delhi", apiKey = apiKey)

                if (geoList.isNotEmpty()) {
                    val lat = geoList[0].lat
                    val lon = geoList[0].lon
                    Log.d("DEBUG", "Calling weather API with lat=$lat, lon=$lon")

                    Log.d("DEBUG", "Coordinates: lat=$lat, lon=$lon")

                    val weather = api.getCurrentWeather(lat, lon, "metric", apiKey)

                    Log.d("DEBUG", "Weather response: $weather")



                    // Set values to UI
                    val tempTextView = findViewById<TextView>(R.id.temperature)
                    tempTextView.text = "${weather.main.temp}°C"

                    val humidity = findViewById<TextView>(R.id.humidity)
                    humidity.text = "${weather.main.humidity}%"

                    val windspeed = findViewById<TextView>(R.id.windspeed)
                    windspeed.text = "${weather.wind.speed}km/h"

                    val feels_like=findViewById<TextView>(R.id.feels_like_value)
                    feels_like.text="${weather.main.feels_like}°C"

                    //weather description
                    val description=findViewById<TextView>(R.id.descriptiontxt)
                    description.text="${weather.weather[0].description}"//make first letter capital
                    //icons
                    val iconCode = weather.weather[0].icon
                    val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
                    val icon=findViewById<ImageView>(R.id.topicon)

                    Glide.with(this@HomeActivity)
                        .load(iconUrl)
                        .into(icon)



                } else {
                    Log.e("DEBUG", "geoList is empty")
                    Toast.makeText(this@HomeActivity, "City not found", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("DEBUG", "API error: ${e.message}")

            }

        }
        //current date
        val dateTextView=findViewById<TextView>(R.id.datetxt)
        val date=SimpleDateFormat("dd , MMMM",Locale.getDefault()).format(Date())
        dateTextView.text=date
        //navigation buttons
        val week_btn=findViewById<ImageButton>(R.id.navWeek)
        week_btn.setOnClickListener {
            val intent = Intent(this, WeeklyActivity::class.java)
            intent.putExtra("city_name", cityName)
            startActivity(intent)

        }
        val settings_btn=findViewById<ImageButton>(R.id.navSettings)
        settings_btn.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)

        }
        val search_btn=findViewById<ImageButton>(R.id.navSearch)
        search_btn.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)

        }




    }
}
