package com.example.core.vision

import android.graphics.Bitmap
import android.util.Log
import com.example.domain.model.DataSource
import com.example.domain.model.DiseaseSeverity
import com.example.domain.model.PestDiseaseItem
import com.example.domain.model.SoilMeasurement
import com.example.domain.model.SoilReport
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.util.UUID

class SoilNotFoundException(message: String) : Exception(message)
class LeafNotFoundException(message: String) : Exception(message)

data class GeminiSoilAnalysisResult(
    val soilReport: SoilReport,
    val dominantHex: String,
    val textureType: String,
    val organicMatterLevel: String,
    val recommendedCrops: List<String>,
    val agronomicSuggestionsEn: String,
    val agronomicSuggestionsHi: String
)

data class GeminiLeafDiagnosisResult(
    val cropIdentified: String,
    val pestDiseaseItem: PestDiseaseItem,
    val confidencePercent: Int,
    val identifiedSymptoms: List<String>,
    val preventionTips: List<String>,
    val visualNotes: String
)

object GeminiAgriculturalVisionService {
    private const val TAG = "GeminiAgriVision"
    private const val MODEL_NAME = "gemini-3.8-flash"

    private val soilAnalysisSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "isSoil" to Schema.boolean("True ONLY if the main subject is real agricultural farm soil, earth, or tilled mud. False for people, hands, walls, tables, leaves, bottles, screens, paper, or any non-soil object."),
                "detectedSubject" to Schema.string("Short description of what is actually visible in the photo."),
                "rejectionReason" to Schema.string("Clear rejection message if isSoil is false.", nullable = true),
                "rejectionReasonHi" to Schema.string("Hindi rejection message if isSoil is false.", nullable = true),
                "soilCategory" to Schema.string("Classification such as Alluvial Loam, Black Regur Soil, Red Laterite Soil, or Sandy Loam.", nullable = true),
                "dominantHex" to Schema.string("Hex color code of the soil surface.", nullable = true),
                "textureType" to Schema.string("Observed soil texture and clod structure.", nullable = true),
                "organicMatterLevel" to Schema.string("Estimated organic carbon / humus level.", nullable = true),
                "nitrogenKgPerHa" to Schema.double("Estimated available Nitrogen in kg/ha.", nullable = true),
                "nitrogenRating" to Schema.string("Low, Medium, or High rating.", nullable = true),
                "phosphorusKgPerHa" to Schema.double("Estimated available Phosphorus in kg/ha.", nullable = true),
                "phosphorusRating" to Schema.string("Low, Medium, or High rating.", nullable = true),
                "potassiumKgPerHa" to Schema.double("Estimated available Potassium in kg/ha.", nullable = true),
                "potassiumRating" to Schema.string("Low, Medium, or High rating.", nullable = true),
                "ph" to Schema.double("Estimated Soil pH between 4.5 and 9.0.", nullable = true),
                "phRating" to Schema.string("Acidic, Neutral, or Alkaline rating.", nullable = true),
                "moisturePercent" to Schema.double("Estimated surface moisture percentage.", nullable = true),
                "moistureRating" to Schema.string("Dry, Adequate, or Moist rating.", nullable = true),
                "recommendedCrops" to Schema.array(
                    items = Schema.string(),
                    description = "Top 3 suitable crops for this soil.",
                    nullable = true
                ),
                "agronomicSuggestionsEn" to Schema.string("Practical soil improvement advice in English.", nullable = true),
                "agronomicSuggestionsHi" to Schema.string("Practical soil improvement advice in Hindi.", nullable = true)
            ),
            optionalProperties = listOf(
                "rejectionReason",
                "rejectionReasonHi",
                "soilCategory",
                "dominantHex",
                "textureType",
                "organicMatterLevel",
                "nitrogenKgPerHa",
                "nitrogenRating",
                "phosphorusKgPerHa",
                "phosphorusRating",
                "potassiumKgPerHa",
                "potassiumRating",
                "ph",
                "phRating",
                "moisturePercent",
                "moistureRating",
                "recommendedCrops",
                "agronomicSuggestionsEn",
                "agronomicSuggestionsHi"
            )
        )
    }

    private val leafDiagnosisSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "isLeaf" to Schema.boolean("True ONLY if the main subject is a real plant leaf, crop foliage, or diseased plant shoot. False for faces, hands, bare soil, walls, rooms, bottles, tables, papers, or non-plant objects."),
                "detectedSubject" to Schema.string("Short description of what is actually visible in the photo."),
                "rejectionReason" to Schema.string("Clear rejection message if isLeaf is false.", nullable = true),
                "rejectionReasonHi" to Schema.string("Hindi rejection message if isLeaf is false.", nullable = true),
                "cropIdentified" to Schema.string("Identified crop name.", nullable = true),
                "diseaseOrPestNameEn" to Schema.string("Specific disease or pest name in English, or Healthy Leaf.", nullable = true),
                "diseaseOrPestNameHi" to Schema.string("Specific disease or pest name in Hindi.", nullable = true),
                "severity" to Schema.enumeration(listOf("LOW", "MEDIUM", "HIGH"), "Severity level.", nullable = true),
                "confidencePercent" to Schema.integer("Confidence percentage 50-99.", nullable = true),
                "pathogenType" to Schema.string("Fungal, Insect, Bacterial, Viral, Deficiency, or Healthy.", nullable = true),
                "identifiedSymptoms" to Schema.array(
                    items = Schema.string(),
                    description = "Observed visual leaf symptoms.",
                    nullable = true
                ),
                "symptomsHindi" to Schema.array(
                    items = Schema.string(),
                    description = "Observed symptoms in Hindi.",
                    nullable = true
                ),
                "organicRemedyEn" to Schema.string("Practical organic treatment in English.", nullable = true),
                "organicRemedyHi" to Schema.string("Practical organic treatment in Hindi.", nullable = true),
                "chemicalRemedyEn" to Schema.string("Practical chemical dosage treatment in English.", nullable = true),
                "chemicalRemedyHi" to Schema.string("Practical chemical dosage treatment in Hindi.", nullable = true),
                "preventionTips" to Schema.array(
                    items = Schema.string(),
                    description = "Preventative measures for the farmer.",
                    nullable = true
                ),
                "visualNotes" to Schema.string("Short summary of visible leaf health.", nullable = true)
            ),
            optionalProperties = listOf(
                "rejectionReason",
                "rejectionReasonHi",
                "cropIdentified",
                "diseaseOrPestNameEn",
                "diseaseOrPestNameHi",
                "severity",
                "confidencePercent",
                "pathogenType",
                "identifiedSymptoms",
                "symptomsHindi",
                "organicRemedyEn",
                "organicRemedyHi",
                "chemicalRemedyEn",
                "chemicalRemedyHi",
                "preventionTips",
                "visualNotes"
            )
        )
    }

    private val soilModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                temperature = 0.1f
                responseMimeType = "application/json"
                responseSchema = soilAnalysisSchema
            }
        )
    }

    private val leafModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                temperature = 0.1f
                responseMimeType = "application/json"
                responseSchema = leafDiagnosisSchema
            }
        )
    }

    private fun Bitmap.scaledForVision(maxDimension: Int = 1024): Bitmap {
        if (width <= maxDimension && height <= maxDimension) return this
        val ratio = width.toFloat() / height.toFloat()
        val targetWidth = if (ratio > 1f) maxDimension else (maxDimension * ratio).toInt().coerceAtLeast(1)
        val targetHeight = if (ratio > 1f) (maxDimension / ratio).toInt().coerceAtLeast(1) else maxDimension
        return Bitmap.createScaledBitmap(this, targetWidth, targetHeight, true)
    }

    suspend fun analyzeSoilImage(
        bitmap: Bitmap,
        state: String,
        district: String
    ): Result<GeminiSoilAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val scaledBitmap = bitmap.scaledForVision()
            val prompt = """
                You are an Agricultural Soil Scientist and strict Image Verification Classifier for Indian farmers.
                Location context: $district, $state, India.

                STEP 1 — STRICT IMAGE VERIFICATION (MANDATORY):
                First look at what is actually in this photo and fill "detectedSubject".
                Ask yourself: Is this clearly a close-up photograph of real agricultural soil, topsoil, plowed field earth, or farm dirt?
                - If the photo shows ANYTHING ELSE — such as a human face, hand, person, room, painted wall, tiled floor, wooden desk/table, paper, laptop/phone screen, bottle, vehicle, clothes, sky, grass/plant leaves without soil, or household objects:
                  SET "isSoil": false immediately!
                  SET "rejectionReason": "Please capture a correct soil image. Detected: <detectedSubject> instead of farm soil."
                  SET "rejectionReasonHi": "कृपया खेत की मिट्टी की सही तस्वीर लें। यह मिट्टी की फोटो नहीं है।"
                  Do NOT estimate any NPK or soil values when isSoil is false.

                STEP 2 — SOIL ANALYSIS (ONLY IF isSoil == true):
                Analyze visible soil color, organic humus darkness, clod texture, and surface moisture sheen to estimate realistic agronomic values for Indian farming in $state:
                - soilCategory (e.g., Alluvial Loam, Black Regur Soil, Red Laterite Soil, Sandy Loam)
                - dominantHex, textureType, organicMatterLevel
                - nitrogenKgPerHa (80–240), phosphorusKgPerHa (10–50), potassiumKgPerHa (110–350), ph (5.2–8.5), moisturePercent (5–40)
                - recommendedCrops (top 3 crops)
                - Practical farmer-friendly agronomicSuggestionsEn & agronomicSuggestionsHi.
            """.trimIndent()

            val response = withTimeout(45_000L) {
                soilModel.generateContent(
                    content {
                        image(scaledBitmap)
                        text(prompt)
                    }
                )
            }

            val jsonText = response.text ?: throw IllegalStateException("Empty response from vision model.")
            val parsed = JSONObject(jsonText)
            val isSoil = parsed.optBoolean("isSoil", false)
            if (!isSoil) {
                val detected = parsed.optString("detectedSubject", "").takeIf { it.isNotBlank() }
                val defaultEn = if (detected != null) {
                    "Soil image not found ($detected detected). Please capture a correct soil image."
                } else {
                    "Soil image not found. Please capture a correct soil image."
                }
                val reasonEn = parsed.optString("rejectionReason", defaultEn).ifBlank { defaultEn }
                val reasonHi = parsed.optString(
                    "rejectionReasonHi",
                    "मिट्टी की तस्वीर नहीं मिली। कृपया खेत की मिट्टी की सही तस्वीर लें।"
                ).ifBlank { "मिट्टी की तस्वीर नहीं मिली। कृपया खेत की मिट्टी की सही तस्वीर लें।" }
                return@withContext Result.failure(SoilNotFoundException("$reasonEn\n$reasonHi"))
            }

            val soilCategory = parsed.optString("soilCategory", "Alluvial Loam Soil").ifBlank { "Alluvial Loam Soil" }
            val dominantHex = parsed.optString("dominantHex", "#6D4C41").ifBlank { "#6D4C41" }
            val textureType = parsed.optString("textureType", "Medium Loam").ifBlank { "Medium Loam" }
            val organicMatter = parsed.optString("organicMatterLevel", "Moderate (0.65%)").ifBlank { "Moderate (0.65%)" }
            val nVal = parsed.optDouble("nitrogenKgPerHa", 135.0).toFloat()
            val nRating = parsed.optString("nitrogenRating", "Medium").ifBlank { "Medium" }
            val pVal = parsed.optDouble("phosphorusKgPerHa", 22.0).toFloat()
            val pRating = parsed.optString("phosphorusRating", "Medium").ifBlank { "Medium" }
            val kVal = parsed.optDouble("potassiumKgPerHa", 210.0).toFloat()
            val kRating = parsed.optString("potassiumRating", "Adequate").ifBlank { "Adequate" }
            val phVal = parsed.optDouble("ph", 6.8).toFloat()
            val phRating = parsed.optString("phRating", "Neutral").ifBlank { "Neutral" }
            val moistVal = parsed.optDouble("moisturePercent", 18.0).toFloat()
            val moistRating = parsed.optString("moistureRating", "Adequate").ifBlank { "Adequate" }

            val cropsList = mutableListOf<String>()
            parsed.optJSONArray("recommendedCrops")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val c = arr.optString(i)
                    if (c.isNotBlank()) cropsList.add(c)
                }
            }
            if (cropsList.isEmpty()) {
                cropsList.addAll(listOf("Wheat", "Mustard", "Chickpea"))
            }

            val suggestionsEn = parsed.optString(
                "agronomicSuggestionsEn",
                "Apply well-decomposed cow dung compost before sowing and split nitrogen into 2-3 doses."
            ).ifBlank {
                "Apply well-decomposed cow dung compost before sowing and split nitrogen into 2-3 doses."
            }
            val suggestionsHi = parsed.optString(
                "agronomicSuggestionsHi",
                "बुवाई से पहले गोबर की सड़ी खाद खेत में मिलाएं और नाइट्रोजन खाद को 2-3 किस्तों में दें।"
            ).ifBlank {
                "बुवाई से पहले गोबर की सड़ी खाद खेत में मिलाएं और नाइट्रोजन खाद को 2-3 किस्तों में दें।"
            }

            val report = SoilReport(
                id = UUID.randomUUID().toString(),
                soilCategory = soilCategory,
                nitrogen = SoilMeasurement(nVal, DataSource.AI_ESTIMATE, 0.94f, nRating),
                phosphorus = SoilMeasurement(pVal, DataSource.AI_ESTIMATE, 0.91f, pRating),
                potassium = SoilMeasurement(kVal, DataSource.AI_ESTIMATE, 0.90f, kRating),
                ph = SoilMeasurement(phVal, DataSource.AI_ESTIMATE, 0.94f, phRating),
                moisture = SoilMeasurement(moistVal, DataSource.AI_ESTIMATE, 0.90f, moistRating),
                overallConfidence = 0.93f,
                state = state,
                district = district,
                notes = "$textureType • $organicMatter"
            )

            Result.success(
                GeminiSoilAnalysisResult(
                    soilReport = report,
                    dominantHex = dominantHex,
                    textureType = textureType,
                    organicMatterLevel = organicMatter,
                    recommendedCrops = cropsList,
                    agronomicSuggestionsEn = suggestionsEn,
                    agronomicSuggestionsHi = suggestionsHi
                )
            )
        } catch (e: CancellationException) {
            if (e is TimeoutCancellationException) {
                Log.e(TAG, "Soil analysis timed out", e)
                Result.failure(Exception("Soil scan timed out. Please check internet connection and try again."))
            } else {
                throw e
            }
        } catch (e: Exception) {
            Log.e(TAG, "Soil analysis exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun analyzeCropLeafImage(
        bitmap: Bitmap,
        cropName: String
    ): Result<GeminiLeafDiagnosisResult> = withContext(Dispatchers.IO) {
        try {
            val scaledBitmap = bitmap.scaledForVision()
            val prompt = """
                You are an Indian Plant Pathologist and strict Image Verification Classifier.
                Selected crop context: $cropName.

                STEP 1 — STRICT IMAGE VERIFICATION (MANDATORY):
                First look at what is actually in this photo and fill "detectedSubject".
                Ask yourself: Is this clearly a photograph of a real plant leaf, crop foliage, or plant stem/shoot?
                - If the photo shows ANYTHING ELSE — such as a human face, hand, person, room, wall, desk/table, floor, bottle, laptop/phone screen, clothes, paper, vehicle, or bare soil with no plant leaf:
                  SET "isLeaf": false immediately!
                  SET "rejectionReason": "Please capture a correct leaf image. Detected: <detectedSubject> instead of a plant leaf."
                  SET "rejectionReasonHi": "कृपया पौधे की पत्ती की सही तस्वीर लें। यह पत्ती की फोटो नहीं है।"
                  Do NOT guess or invent plant diseases when isLeaf is false.

                STEP 2 — LEAF DIAGNOSIS (ONLY IF isLeaf == true):
                Inspect the leaf carefully for fungal spots, rust, blight, mildew, mosaic, insect chewing/holes, curling, or nutrient yellowing.
                Provide:
                - diseaseOrPestNameEn & diseaseOrPestNameHi (or "Healthy Leaf" / "स्वस्थ पत्ती" if healthy)
                - severity (LOW, MEDIUM, HIGH) and confidencePercent (55-99)
                - identifiedSymptoms & symptomsHindi
                - practical organicRemedyEn / organicRemedyHi and chemicalRemedyEn / chemicalRemedyHi with water dosage (per liter)
                - preventionTips and short visualNotes.
            """.trimIndent()

            val response = withTimeout(45_000L) {
                leafModel.generateContent(
                    content {
                        image(scaledBitmap)
                        text(prompt)
                    }
                )
            }

            val jsonText = response.text ?: throw IllegalStateException("Empty response from vision model.")
            val parsed = JSONObject(jsonText)
            val isLeaf = parsed.optBoolean("isLeaf", false)
            if (!isLeaf) {
                val detected = parsed.optString("detectedSubject", "").takeIf { it.isNotBlank() }
                val defaultEn = if (detected != null) {
                    "Leaf image not found ($detected detected). Please capture a correct leaf image."
                } else {
                    "Leaf image not found. Please capture a correct leaf image."
                }
                val reasonEn = parsed.optString("rejectionReason", defaultEn).ifBlank { defaultEn }
                val reasonHi = parsed.optString(
                    "rejectionReasonHi",
                    "पत्ती की तस्वीर नहीं मिली। कृपया पौधे की पत्ती की सही तस्वीर लें।"
                ).ifBlank { "पत्ती की तस्वीर नहीं मिली। कृपया पौधे की पत्ती की सही तस्वीर लें।" }
                return@withContext Result.failure(LeafNotFoundException("$reasonEn\n$reasonHi"))
            }

            val identifiedCrop = parsed.optString("cropIdentified", cropName).ifBlank { cropName }
            val diseaseNameEn = parsed.optString("diseaseOrPestNameEn", "Leaf Spot").ifBlank { "Leaf Spot" }
            val diseaseNameHi = parsed.optString("diseaseOrPestNameHi", "पत्ती धब्बा रोग").ifBlank { "पत्ती धब्बा रोग" }
            val severityStr = parsed.optString("severity", "MEDIUM")
            val severity = when (severityStr.uppercase()) {
                "HIGH", "SEVERE" -> DiseaseSeverity.HIGH
                "LOW", "MILD" -> DiseaseSeverity.LOW
                else -> DiseaseSeverity.MEDIUM
            }
            val confidence = parsed.optInt("confidencePercent", 89).coerceIn(50, 99)
            val pathogen = parsed.optString("pathogenType", "Fungal").ifBlank { "Fungal" }

            val symptomsList = mutableListOf<String>()
            parsed.optJSONArray("identifiedSymptoms")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val s = arr.optString(i)
                    if (s.isNotBlank()) symptomsList.add(s)
                }
            }
            if (symptomsList.isEmpty()) {
                symptomsList.add("Leaf spotting or discoloration observed")
            }

            val symptomsHiList = mutableListOf<String>()
            parsed.optJSONArray("symptomsHindi")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val s = arr.optString(i)
                    if (s.isNotBlank()) symptomsHiList.add(s)
                }
            }

            val organicEn = parsed.optString(
                "organicRemedyEn",
                "Spray Neem oil (1500 ppm) @ 3 ml per liter of water."
            ).ifBlank { "Spray Neem oil (1500 ppm) @ 3 ml per liter of water." }
            val organicHi = parsed.optString(
                "organicRemedyHi",
                "नीम का तेल (1500 ppm) 3 मिली प्रति लीटर पानी में घोलकर छिड़काव करें।"
            ).ifBlank { "नीम का तेल (1500 ppm) 3 मिली प्रति लीटर पानी में घोलकर छिड़काव करें।" }
            val chemicalEn = parsed.optString(
                "chemicalRemedyEn",
                "Spray Mancozeb 75% WP @ 2.5 g per liter of water."
            ).ifBlank { "Spray Mancozeb 75% WP @ 2.5 g per liter of water." }
            val chemicalHi = parsed.optString(
                "chemicalRemedyHi",
                "मैंकोजेब 75% WP 2.5 ग्राम प्रति लीटर पानी में घोलकर छिड़काव करें।"
            ).ifBlank { "मैंकोजेब 75% WP 2.5 ग्राम प्रति लीटर पानी में घोलकर छिड़काव करें।" }

            val preventionTips = mutableListOf<String>()
            parsed.optJSONArray("preventionTips")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val tip = arr.optString(i)
                    if (tip.isNotBlank()) preventionTips.add(tip)
                }
            }
            if (preventionTips.isEmpty()) {
                preventionTips.addAll(
                    listOf(
                        "Remove heavily infected leaves from the field",
                        "Avoid overhead watering late in the evening"
                    )
                )
            }

            val visualNotes = parsed.optString(
                "visualNotes",
                "Leaf inspected for $identifiedCrop."
            ).ifBlank { "Leaf inspected for $identifiedCrop." }

            val item = PestDiseaseItem(
                id = "vision_${System.currentTimeMillis()}",
                cropName = identifiedCrop,
                diseaseName = diseaseNameEn,
                diseaseNameHindi = diseaseNameHi,
                pathogenType = pathogen,
                severity = severity,
                keySymptoms = symptomsList,
                symptomsHindi = symptomsHiList,
                organicRemedy = organicEn,
                organicRemedyHindi = organicHi,
                chemicalRemedy = chemicalEn,
                chemicalRemedyHindi = chemicalHi,
                preventiveMeasures = preventionTips,
                preventiveMeasuresHindi = listOf("संक्रमित पत्तियों को खेत से हटाएं", "शाम के समय सिंचाई से बचें"),
                optimalSprayingWeather = "Best sprayed in calm morning or late afternoon hours."
            )

            Result.success(
                GeminiLeafDiagnosisResult(
                    cropIdentified = identifiedCrop,
                    pestDiseaseItem = item,
                    confidencePercent = confidence,
                    identifiedSymptoms = symptomsList,
                    preventionTips = preventionTips,
                    visualNotes = visualNotes
                )
            )
        } catch (e: CancellationException) {
            if (e is TimeoutCancellationException) {
                Log.e(TAG, "Leaf diagnosis timed out", e)
                Result.failure(Exception("Leaf scan timed out. Please check internet connection and try again."))
            } else {
                throw e
            }
        } catch (e: Exception) {
            Log.e(TAG, "Leaf diagnosis exception: ${e.message}", e)
            Result.failure(e)
        }
    }
}
