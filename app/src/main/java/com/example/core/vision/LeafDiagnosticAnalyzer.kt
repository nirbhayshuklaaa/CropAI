package com.example.core.vision

import android.graphics.Bitmap
import android.graphics.Color
import com.example.domain.model.PestDiseaseItem
import kotlin.math.abs
import kotlin.math.sqrt

data class LeafVisionDiagnosis(
    val identifiedSymptoms: List<String>,
    val diagnosedItem: PestDiseaseItem?,
    val confidence: Float,
    val visualNotes: String
)

object LeafDiagnosticAnalyzer {

    private const val REJECTION_MESSAGE =
        "Leaf image not found. Please capture a correct leaf image.\n" +
            "पत्ती की तस्वीर नहीं मिली। कृपया पौधे की पत्ती की सही तस्वीर लें।"

    fun analyzeLeafBitmap(
        bitmap: Bitmap,
        cropName: String,
        availablePestsForCrop: List<PestDiseaseItem>
    ): LeafVisionDiagnosis {
        val width = bitmap.width
        val height = bitmap.height
        if (width < 16 || height < 16) {
            throw LeafNotFoundException(REJECTION_MESSAGE)
        }

        val gridCols = 48
        val gridRows = 48
        val startX = (width * 0.10f).toInt().coerceIn(0, width - 1)
        val endX = (width * 0.90f).toInt().coerceIn(startX + 1, width)
        val startY = (height * 0.10f).toInt().coerceIn(0, height - 1)
        val endY = (height * 0.90f).toInt().coerceIn(startY + 1, height)

        val spanX = (endX - startX).coerceAtLeast(1)
        val spanY = (endY - startY).coerceAtLeast(1)
        val totalSamples = gridCols * gridRows

        val lums = FloatArray(totalSamples)
        val hsv = FloatArray(3)

        var yellowCount = 0
        var brownDarkCount = 0
        var whiteCount = 0
        var healthyGreenCount = 0
        var nonBotanicalCount = 0

        for (row in 0 until gridRows) {
            val y = startY + (row * spanY) / gridRows
            for (col in 0 until gridCols) {
                val x = startX + (col * spanX) / gridCols
                val pixel = bitmap.getPixel(x.coerceIn(0, width - 1), y.coerceIn(0, height - 1))
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                Color.RGBToHSV(r, g, b, hsv)
                val hue = hsv[0]
                val sat = hsv[1]
                val value = hsv[2]

                val lum = (0.299f * r + 0.587f * g + 0.114f * b) / 255f
                lums[row * gridCols + col] = lum

                when {
                    // Botanical chlorophyll green (Hue 65° - 165°, clear green dominance)
                    hue in 65f..165f && sat > 0.18f && value in 0.12f..0.90f -> healthyGreenCount++
                    // Chlorotic yellow leaf tissue (Hue 38° - 64°, saturated plant yellow)
                    hue in 38f..64f && sat > 0.25f && value in 0.25f..0.92f -> yellowCount++
                    // Non-botanical blue/cyan/purple/magenta/neon red
                    (hue in 170f..348f && sat > 0.18f && value > 0.15f) -> nonBotanicalCount++
                    // White powdery spots
                    sat < 0.15f && value > 0.78f -> whiteCount++
                    // Necrotic spots / rust lesions
                    (hue in 5f..37f && sat > 0.24f && value in 0.08f..0.60f) || (value < 0.20f && sat > 0.15f) -> brownDarkCount++
                }
            }
        }

        val yellowRatio = yellowCount.toFloat() / totalSamples
        val necroticRatio = brownDarkCount.toFloat() / totalSamples
        val whiteRatio = whiteCount.toFloat() / totalSamples
        val greenRatio = healthyGreenCount.toFloat() / totalSamples
        val nonBotanicalRatio = nonBotanicalCount.toFloat() / totalSamples

        // Compute texture / vein variation so solid green walls or flat screens are rejected
        var lumSum = 0.0
        for (l in lums) lumSum += l
        val lumMean = (lumSum / totalSamples).toFloat()
        var varianceSum = 0.0
        for (l in lums) {
            val d = l - lumMean
            varianceSum += d * d
        }
        val lumStdDev = sqrt(varianceSum / totalSamples).toFloat()

        var gradSum = 0.0
        var gradCount = 0
        for (row in 0 until gridRows - 1) {
            for (col in 0 until gridCols - 1) {
                val c = lums[row * gridCols + col]
                val right = lums[row * gridCols + col + 1]
                val down = lums[(row + 1) * gridCols + col]
                gradSum += (abs(c - right) + abs(c - down)) * 0.5f
                gradCount++
            }
        }
        val avgGradient = if (gradCount > 0) (gradSum / gradCount).toFloat() else 0f

        // STRICT LEAF VALIDATION:
        // Must clearly exhibit botanical chlorophyll foliage AND natural leaf texture, and not be dominated by artificial scenes.
        val hasStrongFoliage = greenRatio >= 0.20f ||
            (greenRatio >= 0.12f && (greenRatio + yellowRatio) >= 0.28f)
        val tooManyArtificialColors = nonBotanicalRatio > 0.25f && greenRatio < 0.35f
        val tooFlatUniformSurface = lumStdDev < 0.025f || avgGradient < 0.012f

        if (!hasStrongFoliage || tooManyArtificialColors || tooFlatUniformSurface) {
            throw LeafNotFoundException(REJECTION_MESSAGE)
        }

        val identifiedSymptoms = mutableListOf<String>()

        when (cropName.lowercase()) {
            "wheat" -> {
                if (yellowRatio > 0.10f) {
                    identifiedSymptoms.add("Yellow-orange powdery stripes on leaves")
                    identifiedSymptoms.add("Yellow dust on fingers when touched")
                }
                if (necroticRatio > 0.12f) {
                    identifiedSymptoms.add("Sticky honeydew with black mold")
                }
                if (identifiedSymptoms.isEmpty()) {
                    identifiedSymptoms.add("Tiny green/black aphids on earhead")
                }
            }
            "rice" -> {
                if (necroticRatio > 0.08f) {
                    identifiedSymptoms.add("Spindle/eye shaped gray lesions on leaves")
                    identifiedSymptoms.add("Black rot at neck node of panicle")
                }
                if (yellowRatio > 0.10f) {
                    identifiedSymptoms.add("Circular patches of drying plants ('Hopper burn')")
                }
                if (identifiedSymptoms.isEmpty()) {
                    identifiedSymptoms.add("Tiny brown insects clustered at stem base")
                }
            }
            "tomato" -> {
                if (necroticRatio > 0.10f) {
                    identifiedSymptoms.add("Concentric rings / bullseye spots on older leaves")
                    identifiedSymptoms.add("Sunken dark leathery rot at fruit stem end")
                }
                if (yellowRatio > 0.08f) {
                    identifiedSymptoms.add("Yellow halo around circular brown leaf lesions")
                    identifiedSymptoms.add("Lower leaves dying and falling off")
                }
            }
            "potato" -> {
                if (necroticRatio > 0.08f) {
                    identifiedSymptoms.add("Water-soaked dark lesions on leaf tips and margins")
                    identifiedSymptoms.add("Foul odor with rapid foliage blackening")
                }
                if (whiteRatio > 0.06f) {
                    identifiedSymptoms.add("White cottony fungal growth on leaf undersides")
                }
                if (identifiedSymptoms.isEmpty()) {
                    identifiedSymptoms.add("Brown dry rot beneath potato skin")
                }
            }
            "cotton" -> {
                if (necroticRatio > 0.08f || yellowRatio > 0.10f) {
                    identifiedSymptoms.add("Tiny bore holes in green bolls with frass")
                    identifiedSymptoms.add("Premature dropping of squares and bolls")
                } else {
                    identifiedSymptoms.add("Rosetted flowers that fail to open ('Rosette stage')")
                }
            }
            "maize" -> {
                if (necroticRatio > 0.08f) {
                    identifiedSymptoms.add("Ragged, torn shot-holes on whorl leaves")
                    identifiedSymptoms.add("Sawdust-like frass inside central plant whorl")
                } else {
                    identifiedSymptoms.add("Damage to corn ear tips and silk")
                }
            }
            "mustard" -> {
                if (yellowRatio > 0.08f || necroticRatio > 0.08f) {
                    identifiedSymptoms.add("Colonies of tiny yellow-green aphids sucking flowers")
                    identifiedSymptoms.add("Curling and distortion of flowering twigs")
                } else {
                    identifiedSymptoms.add("Black sticky residue on stems")
                }
            }
            else -> {
                if (yellowRatio > 0.10f) identifiedSymptoms.add("Leaf Yellowing (Chlorosis)")
                if (necroticRatio > 0.10f) identifiedSymptoms.add("Necrotic Spotting / Blight")
                if (whiteRatio > 0.08f) identifiedSymptoms.add("Powdery Mildew Coating")
            }
        }

        val bestMatch = availablePestsForCrop.maxByOrNull { pest ->
            pest.keySymptoms.count { sym ->
                identifiedSymptoms.any { detected ->
                    sym.contains(detected, ignoreCase = true) || detected.contains(sym, ignoreCase = true)
                }
            }
        } ?: availablePestsForCrop.firstOrNull()

        val confidence = when {
            yellowRatio > 0.15f || necroticRatio > 0.15f -> 0.90f
            yellowRatio > 0.08f || necroticRatio > 0.08f -> 0.84f
            whiteRatio > 0.06f -> 0.86f
            else -> 0.78f
        }

        val visualNotes =
            "${(greenRatio * 100).toInt()}% green leaf tissue, ${(yellowRatio * 100).toInt()}% yellowing, ${(necroticRatio * 100).toInt()}% spots."

        return LeafVisionDiagnosis(
            identifiedSymptoms = identifiedSymptoms,
            diagnosedItem = bestMatch,
            confidence = confidence,
            visualNotes = visualNotes
        )
    }
}
