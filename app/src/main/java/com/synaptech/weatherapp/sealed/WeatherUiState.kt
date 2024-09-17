package com.synaptech.weatherapp.sealed

import com.synaptech.weatherapp.model.weather.WeatherResponse

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val weather: WeatherResponse) : WeatherUiState()
    data class Error(val exception: Throwable) : WeatherUiState()
}