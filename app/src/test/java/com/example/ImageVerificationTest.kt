package com.example

import android.graphics.Bitmap
import android.graphics.Color
import com.example.core.vision.LeafDiagnosticAnalyzer
import com.example.core.vision.LeafNotFoundException
import com.example.core.vision.SoilImageAnalyzer
import com.example.core.vision.SoilNotFoundException
import com.example.domain.model.DiseaseSeverity
import com.example.domain.model.PestDiseaseItem
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ImageVerificationTest {

    private val samplePests = listOf(
        PestDiseaseItem(
            id = "wheat_rust",
            cropName = "Wheat",
            diseaseName = "Yellow Rust",
            diseaseNameHindi = "पीला रतुआ",
            pathogenType = "Fungal",
            severity = DiseaseSeverity.HIGH,
            keySymptoms = listOf("Yellow-orange powdery stripes on leaves"),
            symptomsHindi = listOf("पत्तियों पर पीली-नारंगी धारियां"),
            organicRemedy = "Neem oil 3 ml/L",
            organicRemedyHindi = "नीम तेल 3 मिली/लीटर",
            chemicalRemedy = "Propiconazole 25 EC 1 ml/L",
            chemicalRemedyHindi = "प्रोपिकोनाज़ोल 1 मिली/लीटर",
            preventiveMeasures = listOf("Use resistant varieties"),
            preventiveMeasuresHindi = listOf("रोगरोधी किस्म बोएं")
        )
    )

    private fun solidBitmap(color: Int, width: Int = 128, height: Int = 128): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(color)
        return bmp
    }

    private fun texturedSoilBitmap(width: Int = 128, height: Int = 128): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val rng = Random(42)
        for (y in 0 until height) {
            for (x in 0 until width) {
                // Organic granular dark brown soil crumbs with realistic local variance
                val clod = if (((x / 5) + (y / 5)) % 2 == 0) 14 else -14
                val grain = rng.nextInt(-22, 23)
                val r = (108 + clod + grain).coerceIn(30, 165)
                val g = (76 + (clod * 0.7f).toInt() + (grain * 0.8f).toInt()).coerceIn(20, 130)
                val b = (52 + (clod * 0.5f).toInt() + (grain * 0.6f).toInt()).coerceIn(15, 95)
                bmp.setPixel(x, y, Color.rgb(r, g, b))
            }
        }
        return bmp
    }

    private fun texturedLeafBitmap(width: Int = 128, height: Int = 128): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val rng = Random(99)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val isVein = (x % 16 == 0) || (y % 18 == 0)
                val isYellowSpot = x in 42..64 && y in 42..64 && ((x + y) % 3 != 0)
                val jitter = rng.nextInt(-14, 15)
                val color = when {
                    isYellowSpot -> Color.rgb(
                        (212 + jitter).coerceIn(160, 240),
                        (184 + jitter).coerceIn(140, 225),
                        (42 + jitter).coerceIn(15, 80)
                    )
                    isVein -> Color.rgb(
                        (92 + jitter).coerceIn(50, 130),
                        (165 + jitter).coerceIn(120, 210),
                        (64 + jitter).coerceIn(30, 100)
                    )
                    else -> Color.rgb(
                        (46 + jitter).coerceIn(20, 90),
                        (126 + jitter).coerceIn(85, 175),
                        (44 + jitter).coerceIn(18, 85)
                    )
                }
                bmp.setPixel(x, y, color)
            }
        }
        return bmp
    }

    @Test
    fun `soil analyzer rejects non-soil images`() {
        val invalidSubjects = listOf(
            "white_paper" to solidBitmap(Color.rgb(245, 245, 245)),
            "blue_sky" to solidBitmap(Color.rgb(72, 148, 225)),
            "smooth_brown_table" to solidBitmap(Color.rgb(138, 94, 58)),
            "smooth_skin_block" to solidBitmap(Color.rgb(214, 162, 128)),
            "bright_red_object" to solidBitmap(Color.rgb(220, 38, 38)),
            "plant_leaf" to texturedLeafBitmap()
        )

        for ((label, bitmap) in invalidSubjects) {
            try {
                SoilImageAnalyzer.analyzeSoilBitmap(bitmap)
                fail("Expected SoilNotFoundException for $label")
            } catch (e: SoilNotFoundException) {
                assertTrue(
                    "Rejection message should mention soil image for $label",
                    e.message?.contains("soil", ignoreCase = true) == true
                )
            }
        }
    }

    @Test
    fun `soil analyzer accepts genuine textured agricultural soil`() {
        val soilBitmap = texturedSoilBitmap()
        val result = SoilImageAnalyzer.analyzeSoilBitmap(soilBitmap)
        assertNotNull(result.soilReport)
        assertTrue(result.soilReport.soilCategory.isNotBlank())
        assertTrue((result.soilReport.nitrogen?.value ?: 0f) > 0f)
    }

    @Test
    fun `leaf analyzer rejects non-leaf images`() {
        val invalidSubjects = listOf(
            "bare_soil" to texturedSoilBitmap(),
            "white_wall" to solidBitmap(Color.rgb(242, 242, 242)),
            "blue_object" to solidBitmap(Color.rgb(45, 110, 210)),
            "skin_block" to solidBitmap(Color.rgb(212, 158, 124)),
            "flat_uniform_green_screen" to solidBitmap(Color.rgb(40, 160, 60))
        )

        for ((label, bitmap) in invalidSubjects) {
            try {
                LeafDiagnosticAnalyzer.analyzeLeafBitmap(bitmap, "Wheat", samplePests)
                fail("Expected LeafNotFoundException for $label")
            } catch (e: LeafNotFoundException) {
                assertTrue(
                    "Rejection message should mention leaf image for $label",
                    e.message?.contains("leaf", ignoreCase = true) == true
                )
            }
        }
    }

    @Test
    fun `leaf analyzer accepts textured botanical plant leaf`() {
        val leafBitmap = texturedLeafBitmap()
        val diagnosis = LeafDiagnosticAnalyzer.analyzeLeafBitmap(leafBitmap, "Wheat", samplePests)
        assertNotNull(diagnosis.diagnosedItem)
        assertTrue(diagnosis.confidence > 0.5f)
        assertTrue(diagnosis.visualNotes.contains("green", ignoreCase = true))
    }
}
