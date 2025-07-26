package com.sudh.weatherforecast

data class AirQualityResponse(
    val coord: Coordinates,
    val list: List<AirData>
)

data class Coordinates(val lon: Double, val lat: Double)

data class AirData(
    val main: MainAQI,
    val components: Components,
    val dt: Long
)

data class MainAQI(val aqi: Int)

data class Components(
    val co: Double,
    val no: Double,
    val no2: Double,
    val o3: Double,
    val so2: Double,
    val pm2_5: Double,
    val pm10: Double,
    val nh3: Double
)
