package com.synaptech.weatherapp.repository

import com.synaptech.weatherapp.model.coordinate.CoordResponse
import com.synaptech.weatherapp.model.weather.WeatherResponse
import com.synaptech.weatherapp.retrofit.WeatherService
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class WeatherRepositoryTest {

    @Mock
    private lateinit var weatherService: WeatherService

    @Mock
    private lateinit var weatherResponse: WeatherResponse

    private lateinit var weatherRepository: WeatherRepository

    @Before
    fun setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this)

        // Create instance of WeatherRepository with mocked WeatherService
        weatherRepository = WeatherRepository(weatherService)
    }

    @Test
    fun getWeatherWithCorrectResponse() = runTest {
        // Arrange
        val lat = "40.7128"
        val lon = "-74.0060"


        // Mock weatherService response
        whenever(weatherService.getWeather(lat, lon, "e748beade60d9b49a46657debaf81e03"))
            .thenReturn(weatherResponse)

        // Act
        val result = weatherRepository.getWeather(lat, lon)

        // Assert
        assertEquals(weatherResponse, result)
    }

    @Test
    fun getCoordinatesByNamCorrectCoordinates() = runTest {
        // Arrange
        val city = "New York"
        val mockCoordinatesResponse = listOf(CoordResponse("100.0", "100.0"))

        // Mock weatherService response
        whenever(weatherService.getCoordinatesByName(city, 1, "e748beade60d9b49a46657debaf81e03"))
            .thenReturn(mockCoordinatesResponse)

        // Act
        val result = weatherRepository.getCoordinatesByName(city)

        // Assert
        assertEquals(mockCoordinatesResponse, result)
    }
}