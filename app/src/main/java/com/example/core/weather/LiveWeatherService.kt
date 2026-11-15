package com.example.core.weather

import android.util.Log
import com.example.domain.model.WeatherCondition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class LiveWeatherService {

    suspend fun fetchLiveWeather(
        latitude: Double,
        longitude: Double,
        locationName: String
    ): Result<WeatherCondition> = withContext(Dispatchers.IO) {
        try {
            // Open-Meteo provides free, high-accuracy global meteorological data without requiring an API key
            val urlString = String.format(
                Locale.US,
                "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&current=temperature_2m,relative_humidity_2m,precipitation,rain,wind_speed_10m,weather_code",
                latitude,
                longitude
            )

            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 7000
                readTimeout = 7000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "CropAI-Android/1.0")
            }

            if (connection.responseCode !in 200..299) {
                return@withContext Result.failure(
                    Exception("Weather API returned HTTP code ${connection.responseCode}")
                )
            }

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val responseString = reader.use { it.readText() }
            connection.disconnect()

            val json = JSONObject(responseString)
            val current = json.getJSONObject("current")

            val temp = current.getDouble("temperature_2m").toFloat()
            val humidity = current.getDouble("relative_humidity_2m").toFloat()
            val rain = (current.optDouble("rain", current.optDouble("precipitation", 0.0))).toFloat()
            val windSpeed = current.getDouble("wind_speed_10m").toFloat()
            val weatherCode = current.optInt("weather_code", 0)

            val (description, advisory) = interpretWeather(weatherCode, temp, humidity, rain, windSpeed)

            Result.success(
                WeatherCondition(
                    temperatureCelsius = temp,
                    humidityPercent = humidity,
                    rainfallMm = rain,
                    windSpeedKmh = windSpeed,
                    conditionDescription = description,
                    locationName = locationName,
                    agriculturalAdvisory = advisory
                )
            )
        } catch (e: Exception) {
            Log.e("LiveWeatherService", "Failed to fetch live weather: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun interpretWeather(
        code: Int,
        temp: Float,
        humidity: Float,
        rain: Float,
        windSpeed: Float
    ): Pair<String, String> {
        val description = when (code) {
            0 -> "Clear Sky"
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy / Morning Mist"
            51, 53, 55 -> "Light Drizzle"
            61 -> "Slight Rain"
            63 -> "Moderate Rain"
            65 -> "Heavy Rain"
            71, 73, 75 -> "Cold Weather / Hail"
            80, 81, 82 -> "Rain Showers"
            95, 96, 99 -> "Thunderstorm with High Wind"
            else -> if (rain > 1.0f) "Showers & Rain" else "Fair Weather"
        }

        val advisory = when {
            rain > 5.0f -> "Active precipitation detected ($rain mm). Avoid fertilizer broadcasting and postpone chemical pesticide sprays to prevent nutrient runoff."
            rain > 0.0f -> "Light rain present ($rain mm). Good for newly transplanted seedlings; postpone foliar pesticide sprays."
            humidity > 80f && temp > 25f -> "High humidity (${humidity.toInt()}%) and warm temperature promote fungal foliar pathogens (blight, mildew). Monitor leaf undersides closely."
            humidity < 35f && temp > 30f -> "Dry air (${humidity.toInt()}%) and elevated temperature increase evapotranspiration. Schedule drip irrigation or evening watering."
            windSpeed > 25f -> "High wind velocity (${windSpeed.toInt()} km/h). Suspend sprayers to avoid chemical drift onto neighboring fields."
            else -> "Favorable agricultural conditions. Suitable for intercultural operations, field scouting, and routine crop maintenance."
        }

        return description to advisory
    }
}
