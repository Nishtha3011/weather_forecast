package com.sudh.weatherforecast

import com.sudh.weatherforecast.models.ForecastDay

data class ForecastResponse(
    val city: City,
    val list: List<ForecastDay>
)

data class City(
    val name: String,
    val country: String,
    val coord: Coord
)

data class Coord(
    val lat: Double,
    val lon: Double
)

