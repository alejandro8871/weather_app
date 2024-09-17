package com.synaptech.weatherapp.retrofit

import com.synaptech.weatherapp.model.coordinate.CoordResponse
import com.synaptech.weatherapp.model.weather.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {

    //fetch cities by the name, and return lat and lon
    @GET("geo/1.0/direct")
    suspend fun getCoordinatesByName(
        @Query("q") city: String,
        @Query("limit") limit: Int,
        @Query("appid") apiKey: String
    ): List<CoordResponse>

    //fetch weather by the lat and lon, and return weather info
    @GET("data/2.5/weather")
    suspend fun getWeather(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") apiKey: String
    ): WeatherResponse
}