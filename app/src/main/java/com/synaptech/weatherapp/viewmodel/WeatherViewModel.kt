package com.synaptech.weatherapp.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.synaptech.weatherapp.repository.WeatherRepository
import com.synaptech.weatherapp.sealed.WeatherUiState
import com.synaptech.weatherapp.util.SharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationClient: FusedLocationProviderClient, // Inject location client
    application: Application
) : AndroidViewModel(application) {

    private val _weatherData = MutableLiveData<WeatherUiState>()
    val weatherData: LiveData<WeatherUiState> = _weatherData

    private val _permissionGranted = MutableLiveData<Boolean>()
    val permissionGranted: LiveData<Boolean> = _permissionGranted

    fun fetchWeatherData(city: String) {
        viewModelScope.launch {
            try {
                _weatherData.value = WeatherUiState.Loading
                val coordinates = weatherRepository.getCoordinatesByName(city)
                SharedPreferencesManager.saveString("city", city)
                if (coordinates.isNotEmpty()) {
                    val weather = weatherRepository.getWeather(
                        coordinates[0].lat.toString(),
                        coordinates[0].lon.toString()
                    )

                    // Ensure weather is not null
                    _weatherData.value = weather?.let {
                        WeatherUiState.Success(it)
                    } ?: WeatherUiState.Error(NullPointerException("Weather data is null"))
                } else {
                    _weatherData.value =
                        WeatherUiState.Error(IllegalArgumentException("Invalid coordinates"))
                }
            } catch (e: Exception) {
                _weatherData.value = WeatherUiState.Error(e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchWeatherByLocation() {
        locationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                fetchWeatherByLocation(it)
            } ?: run {
                _weatherData.value = WeatherUiState.Error(NullPointerException("Location is null"))
            }
        }
    }

    private fun fetchWeatherByLocation(location: Location?) {
        viewModelScope.launch {
            try {
                _weatherData.value = WeatherUiState.Loading
                val weather = weatherRepository.getWeather(
                    location?.latitude.toString(),
                    location?.longitude.toString()
                )

                _weatherData.value = weather.let {
                    WeatherUiState.Success(it)
                }
            } catch (e: Exception) {
                _weatherData.value = WeatherUiState.Error(e)
            }
        }
    }

    // Check if location permission is granted
    fun checkLocationPermission() {
        val context = getApplication<Application>()
        _permissionGranted.value = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Request permission through activity
    fun requestLocationPermission(locationPermissionLauncher: ActivityResultLauncher<Array<String>>) {
        locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
    }

    // Fetch weather by location if permission is granted
    fun fetchWeatherIfPermissionGranted() {
        if (_permissionGranted.value == true) {
            fetchWeatherByLocation()
        } else {
            val savedCity = SharedPreferencesManager.getString("city", "")
            if (savedCity.isNotEmpty()) {
                fetchWeatherData(savedCity)
            }
        }
    }
}