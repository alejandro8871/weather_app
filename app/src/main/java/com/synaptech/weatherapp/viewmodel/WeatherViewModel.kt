package com.synaptech.weatherapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptech.weatherapp.model.weather.WeatherResponse
import com.synaptech.weatherapp.repository.WeatherRepository
import com.synaptech.weatherapp.util.SharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(private val weatherRepository: WeatherRepository) :
    ViewModel() {
    private val TAG: String = "WeatherViewModel"

    private val _weatherData = MutableLiveData<WeatherResponse>()
    val weatherData: LiveData<WeatherResponse> = _weatherData


    init {
        val savedCity = SharedPreferencesManager.getString("city", "")
        if (savedCity.isNotEmpty()) {
            fetchWeatherData(savedCity)
        }
    }

    fun fetchWeatherData(city: String) {
        viewModelScope.launch {
            try {
                val coordinates = weatherRepository.getCoordinatesByName(city)
                Log.e(TAG, coordinates[0].toString())
                SharedPreferencesManager.saveString("city", city)
                val weather = weatherRepository.getWeather(
                    coordinates[0].lat.toString(),
                    coordinates[0].lon.toString()
                )
                Log.e(TAG, weather.toString())
                _weatherData.value = weather
            } catch (e: Exception) {
                Log.e(TAG, e.printStackTrace().toString())
            }
        }
    }

}