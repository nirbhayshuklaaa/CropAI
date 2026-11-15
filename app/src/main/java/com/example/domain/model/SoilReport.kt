package com.example.domain.model

enum class DataSource {
    AI_ESTIMATE,
    LAB_TESTED,
    USER_ENTERED
}

enum class ImageQualityStatus {
    EXCELLENT,
    ACCEPTABLE,
    BLURRY,
    TOO_DARK,
    TOO_BRIGHT,
    LOW_COVERAGE,
    REJECTED
}

data class SoilMeasurement<T>(
    val value: T,
    val source: DataSource,
    val confidence: Float? = null,
    val interpretation: String? = null
)

data class SoilReport(
    val id: String = "",
    val userId: String = "",
    val fieldId: String? = null,
    val farmName: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val soilCategory: String = "Alluvial Soil",
    val nitrogen: SoilMeasurement<Float>? = null,
    val phosphorus: SoilMeasurement<Float>? = null,
    val potassium: SoilMeasurement<Float>? = null,
    val ph: SoilMeasurement<Float>? = null,
    val moisture: SoilMeasurement<Float>? = null,
    val imageUri: String? = null,
    val imageQuality: ImageQualityStatus? = null,
    val overallConfidence: Float? = null,
    val state: String = "",
    val district: String = "",
    val village: String = "",
    val modelVersion: String = "soil_cnn_v1.2",
    val notes: String = ""
) {
    val isReliableLabBacked: Boolean
        get() = (nitrogen?.source == DataSource.LAB_TESTED ||
                phosphorus?.source == DataSource.LAB_TESTED ||
                potassium?.source == DataSource.LAB_TESTED)
}
