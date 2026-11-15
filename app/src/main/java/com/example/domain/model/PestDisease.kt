package com.example.domain.model

enum class DiseaseSeverity(val label: String, val labelHindi: String) {
    LOW("Mild / प्रारंभिक", "प्रारंभिक / कम"),
    MEDIUM("Moderate / मध्यम", "मध्यम"),
    HIGH("Severe / गंभीर", "गंभीर / आपातकालीन")
}

data class PestDiseaseItem(
    val id: String,
    val cropName: String,
    val diseaseName: String,
    val diseaseNameHindi: String,
    val pathogenType: String, // Fungal, Viral, Bacterial, Insect Pest
    val severity: DiseaseSeverity,
    val keySymptoms: List<String>,
    val symptomsHindi: List<String>,
    val organicRemedy: String,
    val organicRemedyHindi: String,
    val chemicalRemedy: String,
    val chemicalRemedyHindi: String,
    val preventiveMeasures: List<String>,
    val preventiveMeasuresHindi: List<String>,
    val optimalSprayingWeather: String = "Spray during calm morning (wind < 10 km/h, no rain forecast for 24h)"
)

data class DiagnosticResult(
    val matchedDisease: PestDiseaseItem,
    val matchedSymptoms: List<String>,
    val confidencePercentage: Int,
    val photoUri: String? = null,
    val timestampMillis: Long = System.currentTimeMillis()
)
