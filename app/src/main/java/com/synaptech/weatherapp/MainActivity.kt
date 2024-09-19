package com.synaptech.weatherapp

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.synaptech.weatherapp.ui.theme.WeatherAppTheme
import com.synaptech.weatherapp.ui.view.WeatherScreen
import com.synaptech.weatherapp.util.SharedPreferencesManager
import com.synaptech.weatherapp.viewmodel.WeatherViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>
    private val weatherViewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPreferencesManager.init(this)
        enableEdgeToEdge()
        setContent {
            WeatherAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherScreen(Modifier.padding(innerPadding))
                }
            }
        }

        // Register the activity result launcher
        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            weatherViewModel.checkLocationPermission() // Notify ViewModel about permission result
            if (granted) {
                weatherViewModel.fetchWeatherByLocation()
            } else {
                weatherViewModel.fetchWeatherIfPermissionGranted()
            }
        }

        weatherViewModel.checkLocationPermission()
        weatherViewModel.permissionGranted.observe(this) { isGranted ->
            if (isGranted == true) {
                weatherViewModel.fetchWeatherByLocation()
            } else {
                weatherViewModel.requestLocationPermission(locationPermissionLauncher)
            }
        }
    }
}

