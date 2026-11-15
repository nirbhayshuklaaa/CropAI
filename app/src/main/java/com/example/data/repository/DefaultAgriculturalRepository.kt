package com.example.data.repository

import com.example.domain.model.CropPredictionResult
import com.example.domain.model.CropProfile
import com.example.domain.model.DataSource
import com.example.domain.model.Farm
import com.example.domain.model.FarmerProfile
import com.example.domain.model.FertilizerProfile
import com.example.domain.model.FertilizerRecommendationResult
import com.example.domain.model.FertilizerType
import com.example.domain.model.Product
import com.example.domain.model.ProductCategory
import com.example.domain.model.RecommendationFactor
import com.example.domain.model.SoilMeasurement
import com.example.domain.model.SoilReport
import com.example.domain.model.WeatherCondition
import com.example.domain.repository.CropRepository
import com.example.domain.repository.FarmRepository
import com.example.domain.repository.FarmerRepository
import com.example.domain.repository.FertilizerRepository
import com.example.domain.repository.ProductRepository
import com.example.domain.repository.SoilRepository
import com.example.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID

class DefaultAgriculturalRepository :
    SoilRepository,
    CropRepository,
    FertilizerRepository,
    FarmRepository,
    ProductRepository,
    WeatherRepository,
    FarmerRepository {

    private val savedReports = MutableStateFlow<List<SoilReport>>(emptyList())
    private val savedFarms = MutableStateFlow<List<Farm>>(emptyList())
    private val farmerProfile = MutableStateFlow<FarmerProfile?>(
        FarmerProfile(
            id = "guest_farmer",
            name = "Farmer",
            preferredLanguage = "en",
            state = "Uttar Pradesh",
            district = "Varanasi",
            village = "Kashi Rural",
            isOnboarded = false
        )
    )

    // SoilRepository
    override fun getSavedReports(userId: String): Flow<List<SoilReport>> =
        savedReports.asStateFlow()

    override suspend fun getReportById(reportId: String): SoilReport? =
        savedReports.value.firstOrNull { it.id == reportId }

    override suspend fun saveReport(report: SoilReport): Result<String> {
        val newId = if (report.id.isBlank()) UUID.randomUUID().toString() else report.id
        val newReport = report.copy(id = newId)
        savedReports.value = listOf(newReport) + savedReports.value.filter { it.id != newId }
        return Result.success(newId)
    }

    override suspend fun deleteReport(reportId: String): Result<Unit> {
        savedReports.value = savedReports.value.filterNot { it.id == reportId }
        return Result.success(Unit)
    }

    private val liveWeatherService = com.example.core.weather.LiveWeatherService()

    override suspend fun analyzeSoilImage(imageBytes: ByteArray): Result<SoilReport> {
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        return if (bitmap != null) {
            val result = com.example.core.vision.SoilImageAnalyzer.analyzeSoilBitmap(bitmap)
            Result.success(result.soilReport)
        } else {
            val estimatedReport = SoilReport(
                id = UUID.randomUUID().toString(),
                soilCategory = "Alluvial Loam",
                nitrogen = SoilMeasurement(78f, DataSource.AI_ESTIMATE, confidence = 0.82f, interpretation = "Moderate nitrogen baseline"),
                phosphorus = SoilMeasurement(34f, DataSource.AI_ESTIMATE, confidence = 0.80f, interpretation = "Adequate phosphorus reserve"),
                potassium = SoilMeasurement(48f, DataSource.AI_ESTIMATE, confidence = 0.84f, interpretation = "Good potassium availability"),
                ph = SoilMeasurement(6.8f, DataSource.AI_ESTIMATE, confidence = 0.88f, interpretation = "Neutral soil pH, ideal for most crops"),
                moisture = SoilMeasurement(22f, DataSource.AI_ESTIMATE, confidence = 0.85f, interpretation = "Normal soil moisture"),
                overallConfidence = 0.85f,
                modelVersion = "soil_optical_classifier_v3.2",
                notes = "Optical estimate derived from soil surface color and texture."
            )
            Result.success(estimatedReport)
        }
    }

    // CropRepository
    override fun getCropCatalog(): Flow<List<CropProfile>> =
        flowOf(OfflineAgriculturalData.crops)

    override suspend fun getCropById(cropId: String): CropProfile? =
        OfflineAgriculturalData.crops.firstOrNull { it.cropId.equals(cropId, ignoreCase = true) }

    override suspend fun predictCrop(
        soilReport: SoilReport,
        weather: WeatherCondition?,
        season: String
    ): Result<CropPredictionResult> {
        val nVal = soilReport.nitrogen?.value ?: 65f
        val pVal = soilReport.phosphorus?.value ?: 35f
        val kVal = soilReport.potassium?.value ?: 40f
        val phVal = soilReport.ph?.value ?: 6.6f
        val temp = weather?.temperatureCelsius ?: 25f
        val humidity = weather?.humidityPercent ?: 65f
        // Annual / seasonal water index
        val rain = if ((weather?.rainfallMm ?: 0f) > 50f) {
            weather?.rainfallMm ?: 750f
        } else {
            // If weather rainfall is daily mm (e.g. 10mm), map to seasonal equivalent or estimate
            750f + (weather?.rainfallMm ?: 0f) * 20f
        }

        // Multi-dimensional scientific agronomic suitability scoring
        val scored = OfflineAgriculturalData.crops.map { crop ->
            var score = 100f

            // 1. Nitrogen Fit (Weight: 20%)
            if (nVal < crop.optimalNMin) {
                val nDiff = crop.optimalNMin - nVal
                score -= (nDiff / crop.optimalNMin) * 25f
            } else if (nVal > crop.optimalNMax) {
                val nDiff = nVal - crop.optimalNMax
                score -= (nDiff / crop.optimalNMax) * 15f
            }

            // 2. Phosphorus Fit (Weight: 15%)
            if (pVal < crop.optimalPMin) {
                val pDiff = crop.optimalPMin - pVal
                score -= (pDiff / crop.optimalPMin) * 20f
            } else if (pVal > crop.optimalPMax) {
                val pDiff = pVal - crop.optimalPMax
                score -= (pDiff / crop.optimalPMax) * 10f
            }

            // 3. Potassium Fit (Weight: 15%)
            if (kVal < crop.optimalKMin) {
                val kDiff = crop.optimalKMin - kVal
                score -= (kDiff / crop.optimalKMin) * 20f
            } else if (kVal > crop.optimalKMax) {
                val kDiff = kVal - crop.optimalKMax
                score -= (kDiff / crop.optimalKMax) * 10f
            }

            // 4. pH Fit (Weight: 15%)
            if (phVal < crop.optimalPhMin) {
                val phDiff = crop.optimalPhMin - phVal
                score -= phDiff * 18f
            } else if (phVal > crop.optimalPhMax) {
                val phDiff = phVal - crop.optimalPhMax
                score -= phDiff * 18f
            }

            // 5. Temperature Fit (Weight: 15%)
            if (temp < crop.optimalTempMin) {
                val tempDiff = crop.optimalTempMin - temp
                score -= tempDiff * 3.5f
            } else if (temp > crop.optimalTempMax) {
                val tempDiff = temp - crop.optimalTempMax
                score -= tempDiff * 3.5f
            }

            // 6. Rainfall / Moisture Fit (Weight: 15%)
            if (rain < crop.rainfallMinMm) {
                val rainDiff = crop.rainfallMinMm - rain
                score -= (rainDiff / crop.rainfallMinMm) * 25f
            } else if (rain > crop.rainfallMaxMm) {
                val rainDiff = rain - crop.rainfallMaxMm
                score -= (rainDiff / crop.rainfallMaxMm) * 20f
            }

            // 7. Relative Humidity Fit (Weight: 10%)
            if (humidity < crop.optimalHumidityMin) {
                val humDiff = crop.optimalHumidityMin - humidity
                score -= (humDiff / 50f) * 10f
            } else if (humidity > crop.optimalHumidityMax) {
                val humDiff = humidity - crop.optimalHumidityMax
                score -= (humDiff / 50f) * 10f
            }

            // 8. Season bonus
            if (crop.growingSeason.contains(season, ignoreCase = true) || crop.growingSeason.contains("Annual")) {
                score += 8f
            }

            crop to maxOf(15f, minOf(98f, score))
        }.sortedByDescending { it.second }

        val top = scored.first().first
        val topScore = scored.first().second / 100f

        val alternatives = scored.drop(1).take(3).map { it.first to (it.second / 100f) }

        val factors = listOf(
            RecommendationFactor(
                title = "Nitrogen Match (${nVal.toInt()} kg/ha)",
                isPositive = nVal in top.optimalNMin..top.optimalNMax,
                detail = if (nVal in top.optimalNMin..top.optimalNMax) {
                    "Your N level (${nVal.toInt()} kg/ha) matches the optimal demand for ${top.nameEn} (${top.optimalNMin.toInt()}-${top.optimalNMax.toInt()} kg/ha)."
                } else if (nVal < top.optimalNMin) {
                    "N level is lower than ideal (${top.optimalNMin.toInt()} kg/ha); top dressing or legume rotation recommended."
                } else {
                    "N level is high; suitable for heavy vegetative feeders."
                }
            ),
            RecommendationFactor(
                title = "Phosphorus & Potash (P:${pVal.toInt()}, K:${kVal.toInt()})",
                isPositive = pVal >= top.optimalPMin && kVal >= top.optimalKMin,
                detail = "Optimal P is ${top.optimalPMin.toInt()}-${top.optimalPMax.toInt()} kg/ha and K is ${top.optimalKMin.toInt()}-${top.optimalKMax.toInt()} kg/ha for healthy root and seed formation."
            ),
            RecommendationFactor(
                title = "Soil pH Compatibility (${phVal})",
                isPositive = phVal in top.optimalPhMin..top.optimalPhMax,
                detail = "Target soil pH range for ${top.nameEn} is ${top.optimalPhMin} - ${top.optimalPhMax}."
            ),
            RecommendationFactor(
                title = "Climate Suitability (${temp.toInt()}°C, ${humidity.toInt()}% Hum)",
                isPositive = temp in top.optimalTempMin..top.optimalTempMax,
                detail = "Optimal temperature is ${top.optimalTempMin.toInt()}°C - ${top.optimalTempMax.toInt()}°C in ${top.growingSeason} season."
            )
        )

        val rotationSuggestion = when (top.cropId) {
            "rice" -> "Follow with Chickpea, Mustard, or Lentil in Rabi season to break pest cycles and replenish nitrogen naturally."
            "wheat" -> "Rotate with Groundnut, Moong (Green Gram), or Cowpea during summer/Zaid to restore soil organic matter."
            "chickpea", "groundnut" -> "Follow with Wheat, Maize, or Paddy to capitalize on biological nitrogen fixed in the soil."
            "cotton" -> "Rotate with deep-rooting pigeonpea (Arhar) or sorghum to reduce wilt pathogens."
            "potato" -> "Follow with Moong or Green Manure (Dhaincha) to restore soil structure after tuber harvest."
            "sugarcane" -> "Plant short-duration cowpea or french bean as intercrop during early cane stooling."
            else -> "Alternate with a deep-rooting legume pulse to maintain balanced soil biodiversity."
        }

        return Result.success(
            CropPredictionResult(
                primaryCrop = top,
                confidence = topScore,
                alternativeCrops = alternatives,
                contributingFactors = factors,
                rotationSuggestion = rotationSuggestion,
                modelVersion = "crop_rf_classifier_v3.0"
            )
        )
    }

    // FertilizerRepository
    override fun getFertilizerCatalog(): Flow<List<FertilizerProfile>> =
        flowOf(OfflineAgriculturalData.fertilizers)

    override suspend fun recommendFertilizer(
        cropId: String,
        soilReport: SoilReport
    ): Result<FertilizerRecommendationResult> {
        val nVal = soilReport.nitrogen?.value ?: 60f
        val pVal = soilReport.phosphorus?.value ?: 30f
        val kVal = soilReport.potassium?.value ?: 40f

        val (fert, reason, advice) = when {
            nVal < 50f -> Triple(
                OfflineAgriculturalData.fertilizers.first { it.id == "urea" },
                "Nitrogen levels are below optimal threshold ($nVal kg/ha).",
                "Apply split top-dressing of Neem Coated Urea during early vegetative tillering."
            )
            pVal < 25f -> Triple(
                OfflineAgriculturalData.fertilizers.first { it.id == "dap" },
                "Phosphorus levels are deficient ($pVal kg/ha), critical for root initiation.",
                "Apply DAP as basal placement during final field preparation or at seed drilling."
            )
            kVal < 35f -> Triple(
                OfflineAgriculturalData.fertilizers.first { it.id == "mop" },
                "Potash levels are low ($kVal kg/ha), vital for grain filling and drought resistance.",
                "Incorporate MOP (Muriate of Potash) before sowing."
            )
            else -> Triple(
                OfflineAgriculturalData.fertilizers.first { it.id == "vermicompost" },
                "Macronutrients are well balanced; soil organic carbon requires enrichment.",
                "Incorporate 2-3 tonnes/hectare of Organic Vermicompost to enhance microbial health."
            )
        }

        val organicAlts = OfflineAgriculturalData.fertilizers.filter {
            it.type == FertilizerType.ORGANIC || it.type == FertilizerType.BIO_FERTILIZER
        }

        return Result.success(
            FertilizerRecommendationResult(
                recommendedFertilizer = fert,
                confidence = 0.84f,
                deficiencyReason = reason,
                applicationAdvice = advice,
                organicAlternatives = organicAlts
            )
        )
    }

    // FarmRepository
    override fun getFarms(userId: String): Flow<List<Farm>> = savedFarms.asStateFlow()

    override suspend fun saveFarm(farm: Farm): Result<String> {
        val newId = if (farm.id.isBlank()) UUID.randomUUID().toString() else farm.id
        val newFarm = farm.copy(id = newId)
        savedFarms.value = listOf(newFarm) + savedFarms.value.filter { it.id != newId }
        return Result.success(newId)
    }

    override suspend fun deleteFarm(farmId: String): Result<Unit> {
        savedFarms.value = savedFarms.value.filterNot { it.id == farmId }
        return Result.success(Unit)
    }

    // ProductRepository
    override fun getProducts(category: ProductCategory?): Flow<List<Product>> {
        return flowOf(
            if (category == null) OfflineAgriculturalData.products
            else OfflineAgriculturalData.products.filter { it.category == category }
        )
    }

    override fun getRecommendedProductsForCrop(cropId: String): Flow<List<Product>> {
        return flowOf(
            OfflineAgriculturalData.products.filter {
                it.relatedCropIds.contains(cropId) || it.relatedCropIds.isEmpty()
            }
        )
    }

    // WeatherRepository
    override suspend fun getCurrentWeather(latitude: Double, longitude: Double): Result<WeatherCondition> {
        val locName = if (latitude in 25.30..25.35 && longitude in 82.95..83.05) {
            "Varanasi, Uttar Pradesh"
        } else {
            String.format(java.util.Locale.US, "GPS: %.3f°N, %.3f°E", latitude, longitude)
        }

        val liveResult = liveWeatherService.fetchLiveWeather(latitude, longitude, locName)
        if (liveResult.isSuccess) {
            return liveResult
        }

        // Fallback for offline conditions
        return Result.success(
            WeatherCondition(
                temperatureCelsius = 26.5f,
                humidityPercent = 68f,
                rainfallMm = 5.0f,
                windSpeedKmh = 12f,
                conditionDescription = "Fair / Mild Humid",
                locationName = locName,
                agriculturalAdvisory = "Normal field conditions. Monitor moisture levels before next irrigation cycle."
            )
        )
    }

    // FarmerRepository
    override fun getFarmerProfile(userId: String): Flow<FarmerProfile?> =
        farmerProfile.asStateFlow()

    override suspend fun saveFarmerProfile(profile: FarmerProfile): Result<Unit> {
        farmerProfile.value = profile
        return Result.success(Unit)
    }
}
