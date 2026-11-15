package com.example.domain.repository

import com.example.domain.model.CropPredictionResult
import com.example.domain.model.CropProfile
import com.example.domain.model.Farm
import com.example.domain.model.FarmerProfile
import com.example.domain.model.FertilizerProfile
import com.example.domain.model.FertilizerRecommendationResult
import com.example.domain.model.Product
import com.example.domain.model.ProductCategory
import com.example.domain.model.SoilReport
import com.example.domain.model.WeatherCondition
import kotlinx.coroutines.flow.Flow

interface SoilRepository {
    fun getSavedReports(userId: String): Flow<List<SoilReport>>
    suspend fun getReportById(reportId: String): SoilReport?
    suspend fun saveReport(report: SoilReport): Result<String>
    suspend fun deleteReport(reportId: String): Result<Unit>
    suspend fun analyzeSoilImage(imageBytes: ByteArray): Result<SoilReport>
}

interface CropRepository {
    fun getCropCatalog(): Flow<List<CropProfile>>
    suspend fun getCropById(cropId: String): CropProfile?
    suspend fun predictCrop(
        soilReport: SoilReport,
        weather: WeatherCondition?,
        season: String
    ): Result<CropPredictionResult>
}

interface FertilizerRepository {
    fun getFertilizerCatalog(): Flow<List<FertilizerProfile>>
    suspend fun recommendFertilizer(
        cropId: String,
        soilReport: SoilReport
    ): Result<FertilizerRecommendationResult>
}

interface FarmRepository {
    fun getFarms(userId: String): Flow<List<Farm>>
    suspend fun saveFarm(farm: Farm): Result<String>
    suspend fun deleteFarm(farmId: String): Result<Unit>
}

interface ProductRepository {
    fun getProducts(category: ProductCategory?): Flow<List<Product>>
    fun getRecommendedProductsForCrop(cropId: String): Flow<List<Product>>
}

interface WeatherRepository {
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): Result<WeatherCondition>
}

interface FarmerRepository {
    fun getFarmerProfile(userId: String): Flow<FarmerProfile?>
    suspend fun saveFarmerProfile(profile: FarmerProfile): Result<Unit>
}
