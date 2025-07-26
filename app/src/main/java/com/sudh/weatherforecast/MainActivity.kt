package com.sudh.weatherforecast

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var cityInput: AutoCompleteTextView
    private lateinit var nextButton: ImageButton

    private var searchJob: Job? = null
    private var latestCityList: List<String> = emptyList()

    private var userSelectedFromDropdown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //If a city was already selected earlier, go to Home directly
        val prefs = getSharedPreferences("weather_prefs", MODE_PRIVATE)
        val savedCity = prefs.getString("city_name", null)
        if (!savedCity.isNullOrEmpty()) {
            startHome(savedCity)
            return
        }

        setContentView(R.layout.activity_main)
        cityInput = findViewById(R.id.cityInput)
        nextButton = findViewById(R.id.nextButton)

        cityInput.threshold = 1 //show suggestions after 1 char (dheela hai?)
        cityInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.length < 2) return

                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300) //ensuring it doesn't get presses too often

                    try {
                        val api = RetrofitClient.geoApi
                        val results = api.getCoordinatesByCity(query, 5, BuildConfig.OPENWEATHER_API_KEY)

                        latestCityList = results.map {
                            if (it.country.isNotEmpty()) "${it.name}, ${it.country}" else it.name
                        }

                        withContext(Dispatchers.Main) {
                            val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, latestCityList)
                            cityInput.setAdapter(adapter)
                            cityInput.showDropDown()
                        }

                    } catch (e: Exception) {
                        //for whtv reason if fetching fails
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Error fetching suggestions", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        cityInput.setOnEditorActionListener { _, actionId, event ->
            //handles both keyboard enter and and the on-screen enter
            val isDone = actionId == EditorInfo.IME_ACTION_DONE
            val isEnter = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
            if (isDone || isEnter) {
                handleCitySelection()
                true
            } else false
        }

        // Handle item selection from dropdown
        cityInput.setOnItemClickListener { _, _, position, _ ->
            userSelectedFromDropdown = true
            cityInput.dismissDropDown()
        }

        // Show suggestions while typing, only if not selected from dropdown
        cityInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.length < 2) return

                // Reset selection flag because user is typing
                userSelectedFromDropdown = false

                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300)

                    try {
                        val api = RetrofitClient.geoApi
                        val results = api.getCoordinatesByCity(query, 5, BuildConfig.OPENWEATHER_API_KEY)

                        latestCityList = results.map {
                            if (it.country.isNotEmpty()) "${it.name}, ${it.country}" else it.name
                        }

                        withContext(Dispatchers.Main) {
                            if (!userSelectedFromDropdown) { // Show dropdown only if user is typing
                                val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, latestCityList)
                                cityInput.setAdapter(adapter)
                                cityInput.showDropDown()
                            }
                        }

                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Error fetching suggestions", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        nextButton.setOnClickListener { handleCitySelection() }
    }

    private fun handleCitySelection() {
        val input = cityInput.text.toString().trim()

        if (input.isEmpty()) {
            Toast.makeText(this, "Please enter a city", Toast.LENGTH_SHORT).show()
            return
        }

        //make the user select a city from dropdown
        if (!latestCityList.contains(input)) {
            Toast.makeText(this, "Please select a valid city from dropdown", Toast.LENGTH_SHORT).show()
            return
        }

        getSharedPreferences("weather_prefs", MODE_PRIVATE).edit()
            .putString("city_name", input)
            .apply()

        startHome(input)
    }

    private fun startHome(cityName: String) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("city_name", cityName)
        startActivity(intent)
        finish()
    }
}
