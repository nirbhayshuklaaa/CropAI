package com.example.domain.model

data class WeatherCondition(
    val temperatureCelsius: Float,
    val humidityPercent: Float,
    val rainfallMm: Float,
    val windSpeedKmh: Float,
    val conditionDescription: String,
    val locationName: String,
    val agriculturalAdvisory: String
)

data class FarmerProfile(
    val id: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val preferredLanguage: String = "en",
    val state: String = "",
    val district: String = "",
    val village: String = "",
    val isOnboarded: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
