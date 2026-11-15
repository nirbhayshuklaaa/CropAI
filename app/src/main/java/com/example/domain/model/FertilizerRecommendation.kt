package com.example.domain.model

enum class FertilizerType {
    ORGANIC,
    BIO_FERTILIZER,
    CHEMICAL_NPK,
    MICRONUTRIENT,
    SOIL_AMENDMENT
}

data class FertilizerProfile(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val type: FertilizerType,
    val nutrientComposition: String, // e.g. "46-0-0" for Urea, "18-46-0" for DAP
    val description: String,
    val standardGuideline: String,
    val safetyWarning: String
)

data class FertilizerRecommendationResult(
    val recommendedFertilizer: FertilizerProfile,
    val confidence: Float,
    val deficiencyReason: String,
    val applicationAdvice: String,
    val safetyNote: String = "Follow package label and consult your local agricultural extension officer (KVK) for exact plot dosage.",
    val organicAlternatives: List<FertilizerProfile> = emptyList(),
    val modelVersion: String = "fertilizer_decision_tree_v1.4",
    val timestamp: Long = System.currentTimeMillis()
)
