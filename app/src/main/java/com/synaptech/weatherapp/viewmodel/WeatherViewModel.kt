package com.synaptech.weatherapp.viewmodel

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.synaptech.weatherapp.model.weather.WeatherResponse
import com.synaptech.weatherapp.repository.WeatherRepository
import com.synaptech.weatherapp.util.SharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationClient: FusedLocationProviderClient // Inject location client
) : ViewModel() {

    private val TAG: String = "WeatherViewModel"

    private val _weatherData = MutableLiveData<WeatherResponse>()
    val weatherData: LiveData<WeatherResponse> = _weatherData

    fun fetchWeatherData(city: String) {
        viewModelScope.launch {
            try {
                // I like to add all the city that the getCoordinatesByName can fetch if i have time, is possible to do some list with all the cities
                val coordinates = weatherRepository.getCoordinatesByName(city)
                Log.e(TAG, coordinates[0].toString())
                SharedPreferencesManager.saveString("city", city)
                val weather = weatherRepository.getWeather(
                    coordinates[0].lat.toString(),
                    coordinates[0].lon.toString()
                )
                _weatherData.value = weather
            } catch (e: Exception) {
                Log.e(TAG, e.printStackTrace().toString())
            }
        }
    }

    fun fetchWeatherByLocation(location: Location?) {
        viewModelScope.launch {
            try {
                val weather = weatherRepository.getWeather(
                    location?.latitude.toString(),
                    location?.longitude.toString()
                )
                _weatherData.value = weather
            } catch (e: Exception) {
                Log.e(TAG, e.printStackTrace().toString())
            }
        }
    }


    @SuppressLint("MissingPermission")
    fun fetchWeatherByLocation() {
        // Fetch location and weather data
        locationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                fetchWeatherByLocation(location)
            }
        }
    }
}