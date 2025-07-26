package com.sudh.weatherforecast

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",       // Celsius
        @Query("appid") apiKey: String
    ): WeatherResponse

    @GET("data/2.5/forecast/daily")
    suspend fun get7DayForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("cnt") cnt: Int = 9, // Get 9-day forecast
        @Query("units") units: String = "metric" // Celsius
    ): ForecastResponse

    @GET("geo/1.0/direct")
    suspend fun getCoordinatesByCity(
        @Query("q") cityName: String,
        @Query("limit") limit: Int = 5,
        @Query("appid") apiKey: String
    ): List<GeoCodingResponseItem>

    @GET("data/2.5/air_pollution")
    suspend fun getAirQuality(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): AirQualityResponse

}
