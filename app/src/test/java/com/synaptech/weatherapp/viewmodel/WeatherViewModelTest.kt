package com.synaptech.weatherapp.viewmodel

import android.app.Application
import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Task
import com.synaptech.weatherapp.model.coordinate.CoordResponse
import com.synaptech.weatherapp.model.weather.Main
import com.synaptech.weatherapp.model.weather.Weather
import com.synaptech.weatherapp.model.weather.WeatherResponse
import com.synaptech.weatherapp.repository.WeatherRepository
import com.synaptech.weatherapp.sealed.WeatherUiState
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: WeatherViewModel
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var locationClient: FusedLocationProviderClient

    @Before
    fun setUp() {
        // Set the Main dispatcher to the test dispatcher
        Dispatchers.setMain(testDispatcher)

        weatherRepository = mock(WeatherRepository::class.java)
        locationClient = mock(FusedLocationProviderClient::class.java)
        viewModel =
            WeatherViewModel(weatherRepository, locationClient, mock(Application::class.java))
    }

    @After
    fun tearDown() {
        // Reset Main dispatcher after tests
        Dispatchers.resetMain()
    }

    @Test
    fun fetchWeatherDataSuccess() = runTest {
        val mockWeatherResponse = WeatherResponse(
            weather = arrayListOf(
                Weather(
                    id = 1,
                    main = "Clear",
                    description = "clear sky",
                    icon = "01d"
                )
            ),
            main = Main(temp = 25.0),
            name = "New York"
        )

        // Mock repository responses
        whenever(weatherRepository.getCoordinatesByName("New York"))
            .thenReturn(
                listOf(
                    CoordResponse(
                        lat = 40.7128.toString(),
                        lon = (-74.0060).toString()
                    )
                )
            )
        whenever(weatherRepository.getWeather("40.7128", "-74.0060"))
            .thenReturn(mockWeatherResponse)

        // Set up observer
        val observer = mock(Observer::class.java) as Observer<WeatherUiState>
        viewModel.weatherData.observeForever(observer)

        // Execute ViewModel function
        viewModel.fetchWeatherData("New York")

        // Capture LiveData states
        val captor = argumentCaptor<WeatherUiState>()
        verify(observer, times(2)).onChanged(captor.capture())

        viewModel.weatherData.removeObserver(observer)
    }

    @Test
    fun fetchWeatherDataErrorInvalid() = runTest {
        // Mock repository responses
        whenever(weatherRepository.getCoordinatesByName("Invalid City"))
            .thenReturn(emptyList())

        // Set up observer
        val observer = mock(Observer::class.java) as Observer<WeatherUiState>
        viewModel.weatherData.observeForever(observer)

        // Execute ViewModel function
        viewModel.fetchWeatherData("Invalid City")

        // Capture LiveData states
        val captor = argumentCaptor<WeatherUiState>()
        verify(observer, times(2)).onChanged(captor.capture())

        // Assert that the states are Loading -> Error
        val states = captor.allValues
        assert(states[0] is WeatherUiState.Loading)
        assert(states[1] is WeatherUiState.Error)

        viewModel.weatherData.removeObserver(observer)
    }

    @Test
    fun fetchWeatherByLocationSuccess() = runTest {
        val mockLocation = mock(Location::class.java)
        `when`(mockLocation.latitude).thenReturn(40.7128)
        `when`(mockLocation.longitude).thenReturn(-74.0060)

        val mockWeatherResponse = WeatherResponse(
            weather = arrayListOf(
                Weather(
                    id = 1,
                    main = "Clear",
                    description = "clear sky",
                    icon = "01d"
                )
            ),
            main = Main(temp = 25.0),
            name = "New York"
        )

        // Mock repository responses
        whenever(weatherRepository.getWeather("40.7128", "-74.0060"))
            .thenReturn(mockWeatherResponse)

        // Mock Task<Location> to return a successful task with mockLocation
        val mockTask = mock(Task::class.java) as Task<Location>
        whenever(locationClient.lastLocation).thenReturn(mockTask)
        `when`(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val successListener =
                invocation.arguments[0] as com.google.android.gms.tasks.OnSuccessListener<Location>
            successListener.onSuccess(mockLocation)
            mockTask
        }

        // Set up observer
        val observer = mock(Observer::class.java) as Observer<WeatherUiState>
        viewModel.weatherData.observeForever(observer)

        // Execute ViewModel function
        viewModel.fetchWeatherByLocation()

        // Capture LiveData states
        val captor = argumentCaptor<WeatherUiState>()
        verify(observer, times(2)).onChanged(captor.capture())

        viewModel.weatherData.removeObserver(observer)
    }
}