package com.example.domain.model

data class CropProfile(
    val cropId: String,
    val nameEn: String,
    val nameHi: String,
    val nameBn: String,
    val nameMr: String,
    val nameTe: String,
    val nameTa: String,
    val scientificName: String,
    val growingSeason: String, // e.g. "Kharif", "Rabi", "Zaid"
    val durationDays: Int,
    val optimalPhMin: Float,
    val optimalPhMax: Float,
    val optimalTempMin: Float,
    val optimalTempMax: Float,
    val rainfallMinMm: Float,
    val rainfallMaxMm: Float,
    val optimalNMin: Float = 60f,
    val optimalNMax: Float = 120f,
    val optimalPMin: Float = 30f,
    val optimalPMax: Float = 60f,
    val optimalKMin: Float = 25f,
    val optimalKMax: Float = 55f,
    val optimalHumidityMin: Float = 40f,
    val optimalHumidityMax: Float = 80f,
    val waterRequirement: String, // Low, Moderate, High
    val description: String,
    val benefits: List<String> = emptyList(),
    val imageUrl: String = ""
) {
    fun getLocalizedName(languageCode: String): String = when (languageCode.lowercase()) {
        "hi" -> nameHi
        "bn" -> nameBn
        "mr" -> nameMr
        "te" -> nameTe
        "ta" -> nameTa
        else -> nameEn
    }
}

data class RecommendationFactor(
    val title: String,
    val isPositive: Boolean,
    val detail: String
)

data class CropPredictionResult(
    val primaryCrop: CropProfile,
    val confidence: Float,
    val alternativeCrops: List<Pair<CropProfile, Float>>,
    val contributingFactors: List<RecommendationFactor>,
    val rotationSuggestion: String?,
    val modelVersion: String = "crop_rf_classifier_v2.1",
    val timestamp: Long = System.currentTimeMillis()
)
