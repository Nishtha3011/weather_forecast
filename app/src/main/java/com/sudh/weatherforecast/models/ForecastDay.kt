package com.sudh.weatherforecast.models

import com.sudh.weatherforecast.Weather

data class ForecastDay(
    val dt: Long,
    val sunrise: Long,
    val sunset: Long,
    val temp: Temp,
    val feels_like: FeelsLike,
    val pressure: Int,
    val humidity: Int,
    val weather: List<Weather>,
    val speed: Double,
    val deg: Int,
    val clouds: Int,
    val rain: Double? // may be null
)

data class Temp(
    val day: Double,
    val min: Double,
    val max: Double,
    val night: Double,
    val eve: Double,
    val morn: Double
)

data class FeelsLike(
    val day: Double,
    val night: Double,
    val eve: Double,
    val morn: Double
)

data class Info(
    val main: String,
    val description: String,
    val icon: String
)