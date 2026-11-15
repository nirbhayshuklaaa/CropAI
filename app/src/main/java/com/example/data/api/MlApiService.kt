package com.example.data.api

import com.squareup.moshi.Json
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class ApiResponse<T>(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: T?,
    @Json(name = "warnings") val warnings: List<String> = emptyList(),
    @Json(name = "errors") val errors: List<String> = emptyList()
)

data class HealthResponse(
    @Json(name = "status") val status: String,
    @Json(name = "models_loaded") val modelsLoaded: Boolean,
    @Json(name = "version") val version: String
)

data class ImageValidationResponse(
    @Json(name = "is_valid") val isValid: Boolean,
    @Json(name = "quality_status") val qualityStatus: String,
    @Json(name = "blur_score") val blurScore: Float,
    @Json(name = "brightness_score") val brightnessScore: Float,
    @Json(name = "message") val message: String
)

data class SoilAnalysisRequest(
    @Json(name = "nitrogen") val nitrogen: Float?,
    @Json(name = "phosphorus") val phosphorus: Float?,
    @Json(name = "potassium") val potassium: Float?,
    @Json(name = "ph") val ph: Float?,
    @Json(name = "moisture") val moisture: Float?,
    @Json(name = "input_source") val inputSource: String
)

data class SoilAnalysisDto(
    @Json(name = "soil_category") val soilCategory: String,
    @Json(name = "estimated_n") val estimatedN: Float?,
    @Json(name = "estimated_p") val estimatedP: Float?,
    @Json(name = "estimated_k") val estimatedK: Float?,
    @Json(name = "estimated_ph") val estimatedPh: Float?,
    @Json(name = "confidence") val confidence: Float,
    @Json(name = "model_version") val modelVersion: String
)

data class CropPredictionRequest(
    @Json(name = "n") val n: Float,
    @Json(name = "p") val p: Float,
    @Json(name = "k") val k: Float,
    @Json(name = "ph") val ph: Float,
    @Json(name = "temperature") val temperature: Float,
    @Json(name = "humidity") val humidity: Float,
    @Json(name = "rainfall") val rainfall: Float,
    @Json(name = "state") val state: String?,
    @Json(name = "season") val season: String?
)

data class CropRecommendationDto(
    @Json(name = "primary_crop_id") val primaryCropId: String,
    @Json(name = "primary_crop_name") val primaryCropName: String,
    @Json(name = "confidence") val confidence: Float,
    @Json(name = "alternatives") val alternatives: List<Map<String, Any>>,
    @Json(name = "explainability_reasons") val explainabilityReasons: List<String>,
    @Json(name = "rotation_suggestion") val rotationSuggestion: String?
)

data class FertilizerPredictionRequest(
    @Json(name = "crop_id") val cropId: String,
    @Json(name = "soil_type") val soilType: String,
    @Json(name = "n") val n: Float,
    @Json(name = "p") val p: Float,
    @Json(name = "k") val k: Float,
    @Json(name = "ph") val ph: Float
)

data class FertilizerRecommendationDto(
    @Json(name = "recommended_fertilizer") val recommendedFertilizer: String,
    @Json(name = "confidence") val confidence: Float,
    @Json(name = "nutrient_deficiency") val nutrientDeficiency: String,
    @Json(name = "application_guide") val applicationGuide: String,
    @Json(name = "safety_disclaimer") val safetyDisclaimer: String
)

interface MlApiService {
    @GET("health")
    suspend fun checkHealth(): Response<ApiResponse<HealthResponse>>

    @Multipart
    @POST("api/image/validate")
    suspend fun validateSoilImage(
        @Part image: MultipartBody.Part,
        @Header("Authorization") idToken: String?
    ): Response<ApiResponse<ImageValidationResponse>>

    @Multipart
    @POST("api/soil/analyze")
    suspend fun analyzeSoilImage(
        @Part image: MultipartBody.Part,
        @Header("Authorization") idToken: String?
    ): Response<ApiResponse<SoilAnalysisDto>>

    @POST("api/crop/predict")
    suspend fun predictCrop(
        @Body request: CropPredictionRequest,
        @Header("Authorization") idToken: String?
    ): Response<ApiResponse<CropRecommendationDto>>

    @POST("api/fertilizer/predict")
    suspend fun predictFertilizer(
        @Body request: FertilizerPredictionRequest,
        @Header("Authorization") idToken: String?
    ): Response<ApiResponse<FertilizerRecommendationDto>>
}
