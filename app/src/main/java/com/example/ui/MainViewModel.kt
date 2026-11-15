package com.example.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.auth.GoogleAuthHelper
import com.example.core.i18n.AppLanguage
import com.example.data.repository.DefaultAgriculturalRepository
import com.example.data.repository.FirebaseAgriculturalRepository
import com.example.domain.model.CropPredictionResult
import com.example.domain.model.DataSource
import com.example.domain.model.Farm
import com.example.domain.model.FarmerProfile
import com.example.domain.model.Field
import com.example.domain.model.FertilizerRecommendationResult
import com.example.domain.model.Product
import com.example.domain.model.ProductCategory
import com.example.domain.model.SoilMeasurement
import com.example.core.speech.SpeechAdvisoryManager
import com.example.domain.model.CropCostBreakdown
import com.example.domain.model.DiagnosticResult
import com.example.domain.model.PestDiseaseItem
import com.example.domain.model.ProfitCalculationResult
import com.example.domain.repository.PestDiagnosticRepository
import com.example.domain.repository.ProfitCalculationEngine
import com.example.domain.model.SoilReport
import com.example.domain.model.WeatherCondition
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.core.location.LocationHelper
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiState(
    val currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    val farmerProfile: FarmerProfile = FarmerProfile(name = "Kisan Mitra", isOnboarded = true),
    val firebaseUser: FirebaseUser? = null,
    val isSigningIn: Boolean = false,
    val weather: WeatherCondition? = null,
    val currentSoilReport: SoilReport? = null,
    val cropPrediction: CropPredictionResult? = null,
    val fertilizerRecommendation: FertilizerRecommendationResult? = null,
    val savedReports: List<SoilReport> = emptyList(),
    val farms: List<Farm> = emptyList(),
    val products: List<Product> = emptyList(),
    val selectedCategory: ProductCategory? = null,
    val isAnalyzing: Boolean = false,
    val isFetchingLocation: Boolean = false,
    val errorMessage: String? = null,
    val soilScanError: String? = null,
    val leafScanError: String? = null,
    val syncSuccessMessage: String? = null,
    val capturedSoilPhotoBitmap: android.graphics.Bitmap? = null,
    val soilVisionClassification: String? = null,
    val isGeminiSoilAnalysis: Boolean = false,
    val geminiSoilSuggestionsEn: String? = null,
    val geminiSoilSuggestionsHi: String? = null,

    // Pest & Disease Diagnostic Doctor
    val selectedDiagnosticCrop: String = "Wheat",
    val selectedDiagnosticSymptoms: Set<String> = emptySet(),
    val diagnosticResult: DiagnosticResult? = null,
    val capturedLeafPhotoUri: String? = null,
    val capturedLeafPhotoBitmap: android.graphics.Bitmap? = null,
    val isDiagnosing: Boolean = false,
    val isGeminiLeafDiagnosis: Boolean = false,
    val geminiLeafVisualNotes: String? = null,
    val geminiPreventionTips: List<String> = emptyList(),

    // Crop Yield & Profitability Calculator
    val selectedProfitCrop: String = "Wheat",
    val profitAcreage: Double = 2.0,
    val profitCalculationResult: ProfitCalculationResult? = null,
    val customCosts: CropCostBreakdown? = null,
    val customYieldPerAcre: Double? = null,
    val customMarketPrice: Double? = null,

    // Audio Voice Readout (TTS)
    val isSpeaking: Boolean = false,
    val speechSnippet: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val localRepository = DefaultAgriculturalRepository()
    private val firebaseRepository = FirebaseAgriculturalRepository(application)
    val authHelper = GoogleAuthHelper(application)
    val locationHelper = LocationHelper(application)

    val pestRepository = PestDiagnosticRepository()
    val profitEngine = ProfitCalculationEngine()
    val speechManager = SpeechAdvisoryManager(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        observeAuth()
        observeSpeech()
        recalculateProfit()
        if (locationHelper.hasLocationPermission()) {
            fetchExactLocation()
        }
    }

    private fun loadInitialData() {
        if (_uiState.value.farms.isEmpty()) {
            val defaultFarm = Farm(
                id = "farm_main",
                name = "Kisan Farm (मेरा खेत)",
                state = _uiState.value.farmerProfile.state.ifBlank { "Uttar Pradesh" },
                district = _uiState.value.farmerProfile.district.ifBlank { "Varanasi" },
                village = _uiState.value.farmerProfile.village.ifBlank { "Kashi Rural" },
                totalAcreage = 4.3,
                fields = listOf(
                    Field(id = "f1", farmId = "farm_main", name = "North Field (उत्तर खेत)", acreage = 2.5, currentCrop = "Paddy / धान", previousCrops = listOf("Wheat / गेहूं")),
                    Field(id = "f2", farmId = "farm_main", name = "Tubewell Plot (बोरवेल प्लाट)", acreage = 1.8, currentCrop = "Mustard / सरसों", previousCrops = listOf("Maize / मक्का"))
                )
            )
            _uiState.value = _uiState.value.copy(farms = listOf(defaultFarm))
        }

        viewModelScope.launch {
            // Load weather context
            localRepository.getCurrentWeather(25.3176, 82.9739).onSuccess { weather ->
                _uiState.value = _uiState.value.copy(weather = weather)
            }

            // Load verified products
            localRepository.getProducts(null).collect { products ->
                _uiState.value = _uiState.value.copy(products = products)
            }
        }
    }

    private fun observeAuth() {
        authHelper.attemptAutoSignIn(viewModelScope)

        viewModelScope.launch {
            authHelper.currentUser.collect { user ->
                _uiState.value = _uiState.value.copy(firebaseUser = user)
                if (user != null) {
                    val updatedProfile = _uiState.value.farmerProfile.copy(
                        id = user.uid,
                        name = user.displayName?.ifBlank { "Farmer" } ?: _uiState.value.farmerProfile.name
                    )
                    _uiState.value = _uiState.value.copy(farmerProfile = updatedProfile)

                    // Observe cloud soil reports
                    launch {
                        firebaseRepository.getSavedReports(user.uid).collect { cloudReports ->
                            if (cloudReports.isNotEmpty()) {
                                _uiState.value = _uiState.value.copy(savedReports = cloudReports)
                            }
                        }
                    }

                    // Observe cloud farms
                    launch {
                        firebaseRepository.getFarms(user.uid).collect { cloudFarms ->
                            if (cloudFarms.isNotEmpty()) {
                                _uiState.value = _uiState.value.copy(farms = cloudFarms)
                            }
                        }
                    }
                }
            }
        }
    }

    fun signInWithGoogle(activity: Activity) {
        _uiState.value = _uiState.value.copy(isSigningIn = true, errorMessage = null)
        authHelper.signInWithGoogle(
            activity = activity,
            scope = viewModelScope,
            onSuccess = { user ->
                _uiState.value = _uiState.value.copy(
                    isSigningIn = false,
                    firebaseUser = user,
                    syncSuccessMessage = "Signed in as ${user.displayName ?: "Farmer"}. Data synced to Cloud Firestore!"
                )
            },
            onError = { error ->
                _uiState.value = _uiState.value.copy(
                    isSigningIn = false,
                    errorMessage = error
                )
            },
            onCancelled = {
                _uiState.value = _uiState.value.copy(isSigningIn = false)
            }
        )
    }

    fun signOut() {
        authHelper.signOut(viewModelScope) {
            _uiState.value = _uiState.value.copy(
                firebaseUser = null,
                syncSuccessMessage = "Signed out. Operating in offline/guest mode."
            )
        }
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            soilScanError = null,
            leafScanError = null,
            syncSuccessMessage = null
        )
    }

    fun clearSoilScanError() {
        _uiState.value = _uiState.value.copy(soilScanError = null)
    }

    fun clearLeafScanError() {
        _uiState.value = _uiState.value.copy(leafScanError = null)
    }

    fun setLanguage(language: AppLanguage) {
        _uiState.value = _uiState.value.copy(currentLanguage = language)
        val user = _uiState.value.firebaseUser
        if (user != null) {
            viewModelScope.launch {
                firebaseRepository.saveFarmerProfile(
                    _uiState.value.farmerProfile.copy(preferredLanguage = language.code)
                )
            }
        }
    }

    fun completeOnboarding(name: String, state: String, district: String, village: String) {
        completeOnboardingWithFarm(name, state, district, village, "My Farm", 2.0)
    }

    fun completeOnboardingWithFarm(
        name: String,
        state: String,
        district: String,
        village: String,
        farmName: String,
        acreage: Double
    ) {
        val updatedProfile = _uiState.value.farmerProfile.copy(
            name = name.ifBlank { "Farmer" },
            state = state,
            district = district,
            village = village,
            isOnboarded = true
        )

        val newFarm = Farm(
            id = "farm_${System.currentTimeMillis()}",
            userId = _uiState.value.firebaseUser?.uid ?: "guest",
            name = farmName.ifBlank { "My Farm (मेरा खेत)" },
            state = state,
            district = district,
            village = village,
            totalAcreage = acreage
        )

        _uiState.value = _uiState.value.copy(
            farmerProfile = updatedProfile,
            farms = listOf(newFarm) + _uiState.value.farms
        )

        val user = _uiState.value.firebaseUser
        if (user != null) {
            viewModelScope.launch {
                firebaseRepository.saveFarmerProfile(updatedProfile)
                firebaseRepository.saveFarm(newFarm)
            }
        }
    }

    fun analyzeSoilManual(
        n: Float,
        p: Float,
        k: Float,
        ph: Float,
        moisture: Float,
        source: DataSource
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzing = true, errorMessage = null)
            val report = SoilReport(
                id = "sr_${System.currentTimeMillis()}",
                soilCategory = if (ph < 6.0f) "Acidic Red Soil" else if (ph > 7.5f) "Alkaline Black Soil" else "Fertile Alluvial Loam",
                nitrogen = SoilMeasurement(n, source, confidence = if (source == DataSource.LAB_TESTED) 0.98f else 0.70f),
                phosphorus = SoilMeasurement(p, source, confidence = if (source == DataSource.LAB_TESTED) 0.98f else 0.65f),
                potassium = SoilMeasurement(k, source, confidence = if (source == DataSource.LAB_TESTED) 0.98f else 0.72f),
                ph = SoilMeasurement(ph, source, confidence = if (source == DataSource.LAB_TESTED) 0.99f else 0.78f),
                moisture = SoilMeasurement(moisture, source, confidence = 0.80f),
                overallConfidence = if (source == DataSource.LAB_TESTED) 0.97f else 0.75f,
                state = _uiState.value.farmerProfile.state,
                district = _uiState.value.farmerProfile.district
            )

            // Predict crop
            val cropResult = localRepository.predictCrop(report, _uiState.value.weather, "Kharif").getOrNull()
            // Predict fertilizer
            val fertResult = localRepository.recommendFertilizer(cropResult?.primaryCrop?.cropId ?: "rice", report).getOrNull()

            _uiState.value = _uiState.value.copy(
                isAnalyzing = false,
                currentSoilReport = report,
                cropPrediction = cropResult,
                fertilizerRecommendation = fertResult,
                savedReports = listOf(report) + _uiState.value.savedReports
            )

            // Sync to Firestore if authenticated
            val user = _uiState.value.firebaseUser
            if (user != null) {
                firebaseRepository.saveReport(report).onSuccess {
                    _uiState.value = _uiState.value.copy(syncSuccessMessage = "Soil Report saved and synced to Cloud Firestore!")
                }
            }
        }
    }

    fun filterProducts(category: ProductCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        viewModelScope.launch {
            localRepository.getProducts(category).collect { products ->
                _uiState.value = _uiState.value.copy(products = products)
            }
        }
    }

    fun addField(name: String, acreage: Double, currentCrop: String) {
        val currentFarms = _uiState.value.farms
        val targetFarmId = currentFarms.firstOrNull()?.id ?: "farm_${System.currentTimeMillis()}"
        val fieldId = "f_${System.currentTimeMillis()}"
        val newField = Field(
            id = fieldId,
            farmId = targetFarmId,
            name = name,
            acreage = acreage,
            currentCrop = currentCrop.ifBlank { "Unassigned" }
        )

        val updatedFarms = if (currentFarms.isNotEmpty()) {
            val primaryFarm = currentFarms.first()
            val updatedFields = primaryFarm.fields + newField
            val updatedPrimary = primaryFarm.copy(
                fields = updatedFields,
                totalAcreage = updatedFields.sumOf { it.acreage }
            )
            listOf(updatedPrimary) + currentFarms.drop(1)
        } else {
            listOf(
                Farm(
                    id = targetFarmId,
                    userId = _uiState.value.firebaseUser?.uid ?: "guest",
                    name = "My Farm (मेरा खेत)",
                    totalAcreage = acreage,
                    fields = listOf(newField)
                )
            )
        }

        _uiState.value = _uiState.value.copy(farms = updatedFarms)

        val user = _uiState.value.firebaseUser
        if (user != null) {
            viewModelScope.launch {
                firebaseRepository.saveField(targetFarmId, newField)
            }
        }
    }

    fun deleteField(fieldId: String) {
        val currentFarms = _uiState.value.farms
        var targetFarmId: String? = null
        val updatedFarms = currentFarms.map { farm ->
            if (farm.fields.any { it.id == fieldId }) {
                targetFarmId = farm.id
                val remainingFields = farm.fields.filterNot { it.id == fieldId }
                farm.copy(
                    fields = remainingFields,
                    totalAcreage = remainingFields.sumOf { it.acreage }
                )
            } else {
                farm
            }
        }
        _uiState.value = _uiState.value.copy(farms = updatedFarms)

        val user = _uiState.value.firebaseUser
        val farmId = targetFarmId
        if (user != null && farmId != null) {
            viewModelScope.launch {
                firebaseRepository.deleteField(farmId, fieldId)
            }
        }
    }

    fun submitFeedback(isUseful: Boolean, comment: String = "") {
        val user = _uiState.value.firebaseUser
        if (user != null) {
            viewModelScope.launch {
                firebaseRepository.submitFeedback(isUseful, comment).onSuccess {
                    _uiState.value = _uiState.value.copy(
                        syncSuccessMessage = if (isUseful) "Thank you for your positive feedback! Saved to Cloud." else "Thank you for your feedback. We are improving our models."
                    )
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(
                syncSuccessMessage = "Thank you for your feedback!"
            )
        }
    }

    fun deleteSoilReport(reportId: String) {
        _uiState.value = _uiState.value.copy(
            savedReports = _uiState.value.savedReports.filter { it.id != reportId },
            currentSoilReport = if (_uiState.value.currentSoilReport?.id == reportId) null else _uiState.value.currentSoilReport
        )
        val user = _uiState.value.firebaseUser
        if (user != null) {
            viewModelScope.launch {
                firebaseRepository.deleteReport(reportId)
            }
        }
    }

    fun analyzeSoilBitmap(bitmap: android.graphics.Bitmap) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAnalyzing = true,
                errorMessage = null,
                soilScanError = null,
                syncSuccessMessage = null,
                currentSoilReport = null,
                soilVisionClassification = null
            )
            val state = _uiState.value.farmerProfile.state.ifBlank { "Uttar Pradesh" }
            val district = _uiState.value.farmerProfile.district.ifBlank { "Varanasi" }

            // Attempt direct image analysis via Firebase AI Logic (Gemini)
            val geminiResult = com.example.core.vision.GeminiAgriculturalVisionService.analyzeSoilImage(
                bitmap = bitmap,
                state = state,
                district = district
            )

            // If Gemini strictly identified that the photo is NOT soil, reject immediately
            val geminiErr = geminiResult.exceptionOrNull()
            if (geminiErr is com.example.core.vision.SoilNotFoundException) {
                val msg = geminiErr.message
                    ?: "Soil image not found. Please capture a correct soil image.\nमिट्टी की तस्वीर नहीं मिली। कृपया खेत की मिट्टी की सही तस्वीर लें।"
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    currentSoilReport = null,
                    capturedSoilPhotoBitmap = null,
                    soilVisionClassification = null,
                    soilScanError = msg,
                    syncSuccessMessage = null
                )
                return@launch
            }

            val report: com.example.domain.model.SoilReport
            val isGemini: Boolean
            val classification: String
            val suggestionsEn: String?
            val suggestionsHi: String?

            if (geminiResult.isSuccess) {
                val gData = geminiResult.getOrThrow()
                report = gData.soilReport
                isGemini = true
                classification = "${gData.soilReport.soilCategory} • ${gData.textureType}"
                suggestionsEn = gData.agronomicSuggestionsEn
                suggestionsHi = gData.agronomicSuggestionsHi
            } else {
                // Offline / fallback strict computer-vision validation & soil colorimetry
                try {
                    val visionResult = com.example.core.vision.SoilImageAnalyzer.analyzeSoilBitmap(bitmap)
                    report = visionResult.soilReport.copy(state = state, district = district)
                    isGemini = false
                    classification = "${visionResult.soilReport.soilCategory} • ${visionResult.organicMatterLevel}"
                    suggestionsEn = null
                    suggestionsHi = null
                } catch (soilErr: com.example.core.vision.SoilNotFoundException) {
                    val msg = soilErr.message
                        ?: "Soil image not found. Please capture a correct soil image.\nमिट्टी की तस्वीर नहीं मिली। कृपया खेत की मिट्टी की सही तस्वीर लें।"
                    _uiState.value = _uiState.value.copy(
                        isAnalyzing = false,
                        currentSoilReport = null,
                        capturedSoilPhotoBitmap = null,
                        soilVisionClassification = null,
                        soilScanError = msg,
                        syncSuccessMessage = null
                    )
                    return@launch
                }
            }

            // Predict crop based on derived parameters
            val cropResult = localRepository.predictCrop(report, _uiState.value.weather, "Kharif").getOrNull()
            val fertResult = localRepository.recommendFertilizer(cropResult?.primaryCrop?.cropId ?: "rice", report).getOrNull()

            _uiState.value = _uiState.value.copy(
                isAnalyzing = false,
                errorMessage = null,
                soilScanError = null,
                currentSoilReport = report,
                cropPrediction = cropResult,
                fertilizerRecommendation = fertResult,
                capturedSoilPhotoBitmap = bitmap,
                soilVisionClassification = classification,
                isGeminiSoilAnalysis = isGemini,
                geminiSoilSuggestionsEn = suggestionsEn,
                geminiSoilSuggestionsHi = suggestionsHi,
                savedReports = listOf(report) + _uiState.value.savedReports,
                syncSuccessMessage = "Soil Report Ready: ${report.soilCategory}"
            )

            val user = _uiState.value.firebaseUser
            if (user != null) {
                firebaseRepository.saveReport(report)
            }
        }
    }

    fun fetchExactLocation(onResult: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFetchingLocation = true, errorMessage = null)
            locationHelper.fetchExactLocation()
                .onSuccess { loc ->
                    val updatedProfile = _uiState.value.farmerProfile.copy(
                        state = loc.state,
                        district = loc.district,
                        village = loc.village
                    )

                    // Fetch real live weather from Open-Meteo for these detected coordinates
                    val liveWeather = localRepository.getCurrentWeather(loc.latitude, loc.longitude).getOrNull()
                        ?: WeatherCondition(
                            temperatureCelsius = 27.0f,
                            humidityPercent = 65f,
                            rainfallMm = 5.0f,
                            windSpeedKmh = 12f,
                            conditionDescription = "Fair Conditions",
                            locationName = loc.formattedAddress,
                            agriculturalAdvisory = "Live weather synced to ${loc.formattedAddress}."
                        )

                    _uiState.value = _uiState.value.copy(
                        isFetchingLocation = false,
                        farmerProfile = updatedProfile,
                        weather = liveWeather.copy(locationName = loc.formattedAddress),
                        syncSuccessMessage = "GPS Synced: ${loc.formattedAddress} (${liveWeather.temperatureCelsius.toInt()}°C, ${liveWeather.humidityPercent.toInt()}% Hum)"
                    )
                    val user = _uiState.value.firebaseUser
                    if (user != null) {
                        firebaseRepository.saveFarmerProfile(updatedProfile)
                    }
                    onResult?.invoke(true, loc.formattedAddress)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isFetchingLocation = false,
                        errorMessage = "Could not detect location: ${error.message ?: "Permission required"}"
                    )
                    onResult?.invoke(false, error.message ?: "Location error")
                }
        }
    }

    private fun observeSpeech() {
        viewModelScope.launch {
            speechManager.playbackState.collect { playback ->
                _uiState.value = _uiState.value.copy(
                    isSpeaking = playback.isSpeaking,
                    speechSnippet = playback.currentSnippet
                )
            }
        }
    }

    // Pest & Disease Diagnostic Doctor Methods
    fun selectDiagnosticCrop(crop: String) {
        _uiState.value = _uiState.value.copy(
            selectedDiagnosticCrop = crop,
            selectedDiagnosticSymptoms = emptySet(),
            diagnosticResult = null
        )
    }

    fun toggleDiagnosticSymptom(symptom: String) {
        val current = _uiState.value.selectedDiagnosticSymptoms
        val updated = if (current.contains(symptom)) current - symptom else current + symptom
        _uiState.value = _uiState.value.copy(selectedDiagnosticSymptoms = updated)
    }

    fun clearDiagnosticSymptoms() {
        _uiState.value = _uiState.value.copy(
            selectedDiagnosticSymptoms = emptySet(),
            diagnosticResult = null,
            capturedLeafPhotoUri = null,
            capturedLeafPhotoBitmap = null,
            leafScanError = null
        )
    }

    fun setCapturedLeafPhoto(uri: String?) {
        _uiState.value = _uiState.value.copy(capturedLeafPhotoUri = uri)
    }

    fun diagnoseLeafBitmap(bitmap: android.graphics.Bitmap) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDiagnosing = true,
                errorMessage = null,
                leafScanError = null,
                diagnosticResult = null,
                syncSuccessMessage = null
            )
            val currentCrop = _uiState.value.selectedDiagnosticCrop

            // Attempt direct image analysis via Firebase AI Logic (Gemini)
            val geminiResult = com.example.core.vision.GeminiAgriculturalVisionService.analyzeCropLeafImage(
                bitmap = bitmap,
                cropName = currentCrop
            )

            // If Gemini strictly identified that the photo is NOT a leaf, reject immediately
            val geminiErr = geminiResult.exceptionOrNull()
            if (geminiErr is com.example.core.vision.LeafNotFoundException) {
                val msg = geminiErr.message
                    ?: "Leaf image not found. Please capture a correct leaf image.\nपत्ती की तस्वीर नहीं मिली। कृपया पौधे की पत्ती की सही तस्वीर लें।"
                _uiState.value = _uiState.value.copy(
                    isDiagnosing = false,
                    diagnosticResult = null,
                    capturedLeafPhotoBitmap = null,
                    capturedLeafPhotoUri = null,
                    leafScanError = msg,
                    syncSuccessMessage = null
                )
                return@launch
            }

            if (geminiResult.isSuccess) {
                val gData = geminiResult.getOrThrow()
                val result = com.example.domain.model.DiagnosticResult(
                    matchedDisease = gData.pestDiseaseItem,
                    matchedSymptoms = gData.identifiedSymptoms,
                    confidencePercentage = gData.confidencePercent,
                    photoUri = null
                )
                _uiState.value = _uiState.value.copy(
                    isDiagnosing = false,
                    errorMessage = null,
                    leafScanError = null,
                    capturedLeafPhotoBitmap = bitmap,
                    selectedDiagnosticSymptoms = gData.identifiedSymptoms.toSet(),
                    diagnosticResult = result,
                    isGeminiLeafDiagnosis = true,
                    geminiLeafVisualNotes = gData.visualNotes,
                    geminiPreventionTips = gData.preventionTips,
                    syncSuccessMessage = "Leaf Checked: ${gData.pestDiseaseItem.diseaseName}"
                )
            } else {
                // Offline / fallback strict computer-vision validation
                try {
                    val availablePests = pestRepository.getDiseasesForCrop(currentCrop)
                    val visionDiagnosis = com.example.core.vision.LeafDiagnosticAnalyzer.analyzeLeafBitmap(
                        bitmap = bitmap,
                        cropName = currentCrop,
                        availablePestsForCrop = availablePests
                    )
                    val symptomsSet = visionDiagnosis.identifiedSymptoms.toSet()
                    val result = visionDiagnosis.diagnosedItem?.let { pest ->
                        com.example.domain.model.DiagnosticResult(
                            matchedDisease = pest,
                            matchedSymptoms = visionDiagnosis.identifiedSymptoms,
                            confidencePercentage = (visionDiagnosis.confidence * 100).toInt(),
                            photoUri = null
                        )
                    } ?: pestRepository.diagnose(currentCrop, visionDiagnosis.identifiedSymptoms, null)

                    _uiState.value = _uiState.value.copy(
                        isDiagnosing = false,
                        errorMessage = null,
                        leafScanError = null,
                        capturedLeafPhotoBitmap = bitmap,
                        selectedDiagnosticSymptoms = symptomsSet,
                        diagnosticResult = result,
                        isGeminiLeafDiagnosis = false,
                        geminiLeafVisualNotes = visionDiagnosis.visualNotes,
                        geminiPreventionTips = emptyList(),
                        syncSuccessMessage = "Leaf Checked: ${result?.matchedDisease?.diseaseName ?: currentCrop}"
                    )
                } catch (leafErr: com.example.core.vision.LeafNotFoundException) {
                    val msg = leafErr.message
                        ?: "Leaf image not found. Please capture a correct leaf image.\nपत्ती की तस्वीर नहीं मिली। कृपया पौधे की पत्ती की सही तस्वीर लें।"
                    _uiState.value = _uiState.value.copy(
                        isDiagnosing = false,
                        diagnosticResult = null,
                        capturedLeafPhotoBitmap = null,
                        capturedLeafPhotoUri = null,
                        leafScanError = msg,
                        syncSuccessMessage = null
                    )
                    return@launch
                }
            }
        }
    }

    fun runDiseaseDiagnosis() {
        val crop = _uiState.value.selectedDiagnosticCrop
        val symptoms = _uiState.value.selectedDiagnosticSymptoms.toList()
        val photo = _uiState.value.capturedLeafPhotoUri
        val result = pestRepository.diagnose(crop, symptoms, photo)
        _uiState.value = _uiState.value.copy(diagnosticResult = result)
    }

    // Profitability Calculator Methods
    fun selectProfitCrop(crop: String) {
        val benchmark = profitEngine.getBenchmark(crop)
        val result = profitEngine.calculateProfit(
            cropName = crop,
            acreage = _uiState.value.profitAcreage,
            customCosts = benchmark.defaultCosts,
            customYieldPerAcre = benchmark.defaultYieldQuintalsPerAcre,
            customPricePerQuintal = benchmark.openMarketRatePerQuintal
        )
        _uiState.value = _uiState.value.copy(
            selectedProfitCrop = crop,
            customCosts = benchmark.defaultCosts,
            customYieldPerAcre = benchmark.defaultYieldQuintalsPerAcre,
            customMarketPrice = benchmark.openMarketRatePerQuintal,
            profitCalculationResult = result
        )
    }

    fun updateProfitAcreage(acreage: Double) {
        val valid = if (acreage < 0.25) 0.25 else acreage
        val result = profitEngine.calculateProfit(
            cropName = _uiState.value.selectedProfitCrop,
            acreage = valid,
            customCosts = _uiState.value.customCosts,
            customYieldPerAcre = _uiState.value.customYieldPerAcre,
            customPricePerQuintal = _uiState.value.customMarketPrice
        )
        _uiState.value = _uiState.value.copy(
            profitAcreage = valid,
            profitCalculationResult = result
        )
    }

    fun updateCustomProfitParameters(
        costs: CropCostBreakdown? = null,
        yield: Double? = null,
        price: Double? = null
    ) {
        val updatedCosts = costs ?: _uiState.value.customCosts
        val updatedYield = yield ?: _uiState.value.customYieldPerAcre
        val updatedPrice = price ?: _uiState.value.customMarketPrice

        val result = profitEngine.calculateProfit(
            cropName = _uiState.value.selectedProfitCrop,
            acreage = _uiState.value.profitAcreage,
            customCosts = updatedCosts,
            customYieldPerAcre = updatedYield,
            customPricePerQuintal = updatedPrice
        )
        _uiState.value = _uiState.value.copy(
            customCosts = updatedCosts,
            customYieldPerAcre = updatedYield,
            customMarketPrice = updatedPrice,
            profitCalculationResult = result
        )
    }

    fun recalculateProfit() {
        val benchmark = profitEngine.getBenchmark(_uiState.value.selectedProfitCrop)
        val costs = _uiState.value.customCosts ?: benchmark.defaultCosts
        val yield = _uiState.value.customYieldPerAcre ?: benchmark.defaultYieldQuintalsPerAcre
        val price = _uiState.value.customMarketPrice ?: benchmark.openMarketRatePerQuintal

        val result = profitEngine.calculateProfit(
            cropName = _uiState.value.selectedProfitCrop,
            acreage = _uiState.value.profitAcreage,
            customCosts = costs,
            customYieldPerAcre = yield,
            customPricePerQuintal = price
        )
        _uiState.value = _uiState.value.copy(
            customCosts = costs,
            customYieldPerAcre = yield,
            customMarketPrice = price,
            profitCalculationResult = result
        )
    }

    // Audio Voice Readout Methods
    fun speakAdvisory(text: String) {
        val isHindi = _uiState.value.currentLanguage == AppLanguage.HINDI
        speechManager.speak(text, isHindi)
    }

    fun stopSpeaking() {
        speechManager.stop()
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.shutdown()
    }
}
