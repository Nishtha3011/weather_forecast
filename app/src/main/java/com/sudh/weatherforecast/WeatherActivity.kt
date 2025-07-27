package com.sudh.weatherforecast

import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

import androidx.appcompat.app.AppCompatActivity

class WeatherActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        val adapter = TabPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.setIcon(R.drawable.ic_home)
                1 -> tab.setIcon(R.drawable.ic_week)
                2 -> tab.setIcon(R.drawable.ic_search)
                3 -> tab.setIcon(R.drawable.ic_settings)
            }
        }.attach()

        val cityName = intent.getStringExtra("city_name") ?: "Delhi"

        val prefs = getSharedPreferences("weather_prefs", MODE_PRIVATE)
        prefs.edit().putString("city_name", cityName).apply()

    }

    fun switchToPage(index: Int) {
        viewPager.currentItem = index
    }
}