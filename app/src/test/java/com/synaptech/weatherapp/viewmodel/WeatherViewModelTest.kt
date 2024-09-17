package com.synaptech.weatherapp.viewmodel

import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.synaptech.weatherapp.model.coordinate.CoordResponse
import com.synaptech.weatherapp.model.weather.WeatherResponse
import com.synaptech.weatherapp.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever


@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: WeatherViewModel

    // Mocks
    @Mock
    private lateinit var weatherRepository: WeatherRepository

    @Mock
    private lateinit var locationClient: FusedLocationProviderClient

    @Mock
    private lateinit var weatherObserver: Observer<WeatherResponse>

    @Mock
    private lateinit var mockWeatherResponse: WeatherResponse

    @Mock
    private lateinit var mockLocation: Location

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        // Initialize ViewModel
        viewModel = WeatherViewModel(weatherRepository, locationClient)
        viewModel.weatherData.observeForever(weatherObserver)
    }

    @After
    fun tearDown() {
        // Remove the test dispatcher
        Dispatchers.resetMain()
    }

    @Test
    fun fetchWeatherDataFetchWeather() = runTest {
        // Arrange
        val city = "New York"
        val mockCoordinates = listOf(
            CoordResponse("40.7128", "-74.0060")
        )

        // Mock the repository's response for coordinates and weather
        whenever(weatherRepository.getCoordinatesByName(city)).thenReturn(mockCoordinates)
        whenever(
            weatherRepository.getWeather(
                eq("40.7128"), eq("-74.0060")
            )
        ).thenReturn(mockWeatherResponse)

        // Act
        viewModel.fetchWeatherData(city)

        // Assert
        verify(weatherRepository).getCoordinatesByName(city)
        verify(weatherRepository).getWeather(eq("40.7128"), eq("-74.0060"))
        verify(weatherObserver).onChanged(mockWeatherResponse)
    }

    @Test
    fun fetchWeatherByLocationWeatherLocation() = runTest {
        // Arrange
        `when`(mockLocation.latitude).thenReturn(40.7128)
        `when`(mockLocation.longitude).thenReturn(-74.0060)

        // Mock the repository's weather response
        whenever(
            weatherRepository.getWeather(
                eq("40.7128"),
                eq("-74.0060")
            )
        ).thenReturn(mockWeatherResponse)

        // Act
        viewModel.fetchWeatherByLocation()

        // Assert
        verify(weatherRepository).getWeather(eq("40.7128"), eq("-74.0060"))
        verify(weatherObserver).onChanged(mockWeatherResponse)
    }

    @Test
    fun fetchWeatherByLocationNotUpdate() = runTest {
        // Arrange
        `when`(mockLocation.latitude).thenReturn(0.0)
        `when`(mockLocation.longitude).thenReturn(0.0)

        // Act
        viewModel.fetchWeatherByLocation() // Ensure fetchWeatherByLocation method is called

        // Assert
        verify(weatherObserver, never()).onChanged(any())
    }
}