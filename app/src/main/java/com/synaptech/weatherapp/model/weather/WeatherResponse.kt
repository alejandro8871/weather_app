package com.synaptech.weatherapp.model.weather

data class WeatherResponse(
    var weather: ArrayList<Weather> = arrayListOf(),
    var main: Main? = Main(),
    var name: String
)
