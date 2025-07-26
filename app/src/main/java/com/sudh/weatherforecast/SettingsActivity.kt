package com.sudh.weatherforecast

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences


    private lateinit var spinnerTempUnit: Spinner
    private lateinit var spinnerWindUnit: Spinner
    private lateinit var spinnerAirPressureUnit: Spinner
    private lateinit var spinnerVisibilityUnit: Spinner
    private lateinit var spinnerThemeMode: Spinner
    private lateinit var spinnerBackgroundType: Spinner

    private lateinit var switchSevereWarnings: SwitchCompat
    private lateinit var  switchRainAlerts: SwitchCompat
    private lateinit var switchDailyForecast: SwitchCompat

    companion object {
        const val PREFS_NAME = "AppSettings"

        const val KEY_TEMP_UNIT = "temperature_unit"
        const val KEY_WIND_UNIT = "wind_unit"
        const val KEY_AIR_PRESSURE_UNIT = "air_pressure_unit"
        const val KEY_VISIBILITY_UNIT = "visibility_unit"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_BACKGROUND_TYPE = "background_type"

        const val KEY_SEVERE_WARNINGS_ENABLED = "severe_warnings_enabled"
        const val KEY_RAIN_ALERTS_ENABLED = "rain_alerts_enabled"
        const val KEY_DAILY_FORECAST_ENABLED = "daily_forecast_enabled"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val backButton: ImageView = findViewById(R.id.backButton)


        spinnerTempUnit = findViewById(R.id.spinnerTempUnit)
        spinnerWindUnit = findViewById(R.id.spinnerWindUnit)
        spinnerAirPressureUnit = findViewById(R.id.spinnerAirPressureUnit)
        spinnerVisibilityUnit = findViewById(R.id.spinnerVisibilityUnit)
        spinnerThemeMode = findViewById(R.id.spinnerThemeMode)
        spinnerBackgroundType = findViewById(R.id.spinnerBackgroundType)


        switchSevereWarnings = findViewById(R.id.switchSevereWarnings)
        switchRainAlerts = findViewById(R.id.switchRainAlerts)
        switchDailyForecast = findViewById(R.id.switchDailyForecast)


        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        setupSpinner(spinnerTempUnit, R.array.temperature_units, KEY_TEMP_UNIT)
        setupSpinner(spinnerWindUnit, R.array.wind_units, KEY_WIND_UNIT)
        setupSpinner(spinnerAirPressureUnit, R.array.air_pressure_units, KEY_AIR_PRESSURE_UNIT)
        setupSpinner(spinnerVisibilityUnit, R.array.visibility_units, KEY_VISIBILITY_UNIT)
        setupSpinner(spinnerThemeMode, R.array.theme_modes, KEY_THEME_MODE)
        setupSpinner(spinnerBackgroundType, R.array.background_types, KEY_BACKGROUND_TYPE)


        setupSwitch(switchSevereWarnings, KEY_SEVERE_WARNINGS_ENABLED)
        setupSwitch(switchRainAlerts, KEY_RAIN_ALERTS_ENABLED)
        setupSwitch(switchDailyForecast, KEY_DAILY_FORECAST_ENABLED)
    }

    private fun setupSpinner(spinner: Spinner, arrayResId: Int, prefKey: String) {
        val adapter = ArrayAdapter.createFromResource(
            this,
            arrayResId,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        spinner.adapter = adapter

        val savedPosition = sharedPreferences.getInt(prefKey, 0)
        spinner.setSelection(savedPosition)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                sharedPreferences.edit().putInt(prefKey, position).apply()


                val selectedItem = parent.getItemAtPosition(position).toString()
                Toast.makeText(this@SettingsActivity, "${prefKey.formatKey()}: $selectedItem", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }

    private fun setupSwitch(switchCompat: SwitchCompat, prefKey: String) {
        // Load saved state
        val savedState = sharedPreferences.getBoolean(prefKey, false)
        switchCompat.isChecked = savedState

        switchCompat.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean(prefKey, isChecked).apply()
            val status = if (isChecked) "Enabled" else "Disabled"
            Toast.makeText(this, "${prefKey.formatKey()}: $status", Toast.LENGTH_SHORT).show()
        }
    }

    private fun String.formatKey(): String {
        return this.replace("_", " ")
            .split(" ")
            .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } } // Capitalize first letter of each word
    }
}