package com.example

import com.example.core.i18n.AppLanguage
import com.example.core.i18n.TranslationKey
import com.example.core.i18n.Translations
import com.example.data.repository.DefaultAgriculturalRepository
import com.example.data.repository.OfflineAgriculturalData
import com.example.domain.model.DataSource
import com.example.domain.model.SoilMeasurement
import com.example.domain.model.SoilReport
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CropAiArchitectureTest {

    private val repository = DefaultAgriculturalRepository()

    @Test
    fun testAllSixLanguagesSupported() {
        val expectedLanguages = listOf(
            AppLanguage.ENGLISH,
            AppLanguage.HINDI,
            AppLanguage.BENGALI,
            AppLanguage.MARATHI,
            AppLanguage.TELUGU,
            AppLanguage.TAMIL
        )
        assertEquals(6, expectedLanguages.size)

        for (lang in expectedLanguages) {
            val appTitle = Translations.get(TranslationKey.APP_NAME, lang)
            assertNotNull(appTitle)
            assertFalse(appTitle.isBlank())

            val heroAction = Translations.get(TranslationKey.ANALYZE_SOIL_HERO, lang)
            assertNotNull(heroAction)
            assertFalse(heroAction.isBlank())

            val safety = Translations.get(TranslationKey.FERTILIZER_SAFETY_NOTE, lang)
            assertNotNull(safety)
            assertFalse(safety.isBlank())

            val step1 = Translations.get(TranslationKey.ONBOARDING_STEP_1_TITLE, lang)
            assertNotNull(step1)
            assertFalse(step1.isBlank())

            val step5 = Translations.get(TranslationKey.ONBOARDING_STEP_5_TITLE, lang)
            assertNotNull(step5)
            assertFalse(step5.isBlank())

            val complete = Translations.get(TranslationKey.COMPLETE_SETUP, lang)
            assertNotNull(complete)
            assertFalse(complete.isBlank())
        }
    }

    @Test
    fun testOfflineCropCatalogCompleteness() {
        val crops = OfflineAgriculturalData.crops
        assertTrue("Crop catalog must have major Indian crops", crops.size >= 8)

        val cropIds = crops.map { it.cropId }
        assertTrue(cropIds.contains("rice"))
        assertTrue(cropIds.contains("wheat"))
        assertTrue(cropIds.contains("cotton"))
        assertTrue(cropIds.contains("chickpea"))

        for (crop in crops) {
            assertTrue("Crop must have optimal pH range", crop.optimalPhMin < crop.optimalPhMax)
            assertTrue("Crop must have duration", crop.durationDays > 60)
            assertFalse("Crop must have English name", crop.nameEn.isBlank())
            assertFalse("Crop must have Hindi name", crop.nameHi.isBlank())
        }
    }

    @Test
    fun testCropPredictionAndExplainability() = runBlocking {
        val soil = SoilReport(
            soilCategory = "Alluvial Loam",
            nitrogen = SoilMeasurement(80f, DataSource.LAB_TESTED),
            phosphorus = SoilMeasurement(35f, DataSource.LAB_TESTED),
            potassium = SoilMeasurement(45f, DataSource.LAB_TESTED),
            ph = SoilMeasurement(6.5f, DataSource.LAB_TESTED)
        )

        val result = repository.predictCrop(soil, null, "Kharif").getOrThrow()
        assertNotNull(result.primaryCrop)
        assertTrue(result.confidence > 0.0f)
        assertTrue(result.contributingFactors.isNotEmpty())
        assertNotNull(result.rotationSuggestion)
    }

    @Test
    fun testFertilizerSafetyAndRecommendation() = runBlocking {
        val lowNitrogenSoil = SoilReport(
            nitrogen = SoilMeasurement(35f, DataSource.LAB_TESTED),
            phosphorus = SoilMeasurement(30f, DataSource.LAB_TESTED),
            potassium = SoilMeasurement(40f, DataSource.LAB_TESTED),
            ph = SoilMeasurement(6.8f, DataSource.LAB_TESTED)
        )

        val result = repository.recommendFertilizer("rice", lowNitrogenSoil).getOrThrow()
        assertNotNull(result.recommendedFertilizer)
        assertTrue(result.recommendedFertilizer.id == "urea")
        assertTrue(result.safetyNote.contains("KVK") || result.safetyNote.contains("label"))
        assertFalse(result.deficiencyReason.isBlank())
    }
}
