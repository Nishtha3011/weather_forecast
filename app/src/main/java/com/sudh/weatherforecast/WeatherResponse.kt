package com.sudh.weatherforecast

data class WeatherResponse(
    val name: String,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind
)

data class Main(
    val temp: Double,
    val feels_like: Double,
    val pressure: Int,
    val humidity: Int,
    val temp_min:Double,
    val temp_max:Double

)

data class Weather(
    val main: String,
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double,
    val deg: Int
)
