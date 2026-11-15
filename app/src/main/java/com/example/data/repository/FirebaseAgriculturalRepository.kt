package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.domain.model.DataSource
import com.example.domain.model.Farm
import com.example.domain.model.FarmerProfile
import com.example.domain.model.Field
import com.example.domain.model.SoilMeasurement
import com.example.domain.model.SoilReport
import com.example.domain.repository.FarmRepository
import com.example.domain.repository.FarmerRepository
import com.example.domain.repository.SoilRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseAgriculturalRepository(
    private val firestore: FirebaseFirestore
) : SoilRepository, FarmRepository, FarmerRepository {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.getString(R.string.firestore_database_id)
        )
    )

    private fun currentUserId(): String? = Firebase.auth.currentUser?.uid

    // SOIL REPORTS
    override fun getSavedReports(userId: String): Flow<List<SoilReport>> {
        val uid = userId.ifBlank { currentUserId() ?: "" }
        if (uid.isBlank()) return flowOf(emptyList())

        return firestore.collection("users").document(uid).collection("soil_reports")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    val nVal = (data["nitrogen"] as? Number)?.toFloat()
                    val pVal = (data["phosphorus"] as? Number)?.toFloat()
                    val kVal = (data["potassium"] as? Number)?.toFloat()
                    val phVal = (data["ph"] as? Number)?.toFloat()
                    val mVal = (data["moisture"] as? Number)?.toFloat()
                    val conf = (data["confidence"] as? Number)?.toFloat()

                    val srcStr = data["source"] as? String ?: "LAB_TESTED"
                    val source = try { DataSource.valueOf(srcStr) } catch (_: Exception) { DataSource.LAB_TESTED }

                    SoilReport(
                        id = doc.id,
                        userId = uid,
                        soilCategory = data["soilCategory"] as? String ?: "Alluvial Soil",
                        nitrogen = nVal?.let { SoilMeasurement(it, source, conf) },
                        phosphorus = pVal?.let { SoilMeasurement(it, source, conf) },
                        potassium = kVal?.let { SoilMeasurement(it, source, conf) },
                        ph = phVal?.let { SoilMeasurement(it, source, conf) },
                        moisture = mVal?.let { SoilMeasurement(it, source, conf) },
                        overallConfidence = conf,
                        timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    )
                }
            }
            .catch { e ->
                Log.e("FirebaseAgriRepo", "Error observing soil reports: ${e.message}", e)
                emit(emptyList())
            }
    }

    override suspend fun getReportById(reportId: String): SoilReport? {
        val uid = currentUserId() ?: return null
        return try {
            val doc = firestore.collection("users").document(uid).collection("soil_reports").document(reportId).get().await()
            if (!doc.exists()) return null
            val data = doc.data ?: return null
            val nVal = (data["nitrogen"] as? Number)?.toFloat()
            val pVal = (data["phosphorus"] as? Number)?.toFloat()
            val kVal = (data["potassium"] as? Number)?.toFloat()
            val phVal = (data["ph"] as? Number)?.toFloat()
            val mVal = (data["moisture"] as? Number)?.toFloat()
            val conf = (data["confidence"] as? Number)?.toFloat()

            val srcStr = data["source"] as? String ?: "LAB_TESTED"
            val source = try { DataSource.valueOf(srcStr) } catch (_: Exception) { DataSource.LAB_TESTED }

            SoilReport(
                id = doc.id,
                userId = uid,
                soilCategory = data["soilCategory"] as? String ?: "Alluvial Soil",
                nitrogen = nVal?.let { SoilMeasurement(it, source, conf) },
                phosphorus = pVal?.let { SoilMeasurement(it, source, conf) },
                potassium = kVal?.let { SoilMeasurement(it, source, conf) },
                ph = phVal?.let { SoilMeasurement(it, source, conf) },
                moisture = mVal?.let { SoilMeasurement(it, source, conf) },
                overallConfidence = conf
            )
        } catch (e: Exception) {
            Log.e("FirebaseAgriRepo", "Failed to fetch report $reportId", e)
            null
        }
    }

    override suspend fun saveReport(report: SoilReport): Result<String> {
        val uid = currentUserId() ?: return Result.failure(IllegalStateException("Farmer must be signed in with Google to sync to cloud."))
        val docId = if (report.id.isNotBlank()) report.id else UUID.randomUUID().toString()

        val payload = mapOf(
            "id" to docId,
            "userId" to uid,
            "soilCategory" to report.soilCategory,
            "nitrogen" to (report.nitrogen?.value ?: 0f),
            "phosphorus" to (report.phosphorus?.value ?: 0f),
            "potassium" to (report.potassium?.value ?: 0f),
            "ph" to (report.ph?.value ?: 7.0f),
            "moisture" to (report.moisture?.value ?: 20f),
            "source" to (report.nitrogen?.source?.name ?: DataSource.LAB_TESTED.name),
            "confidence" to (report.overallConfidence ?: 0.85f),
            "createdAt" to FieldValue.serverTimestamp()
        )

        return try {
            firestore.collection("users").document(uid).collection("soil_reports").document(docId).set(payload).await()
            Result.success(docId)
        } catch (e: Exception) {
            Log.e("FirebaseAgriRepo", "Failed to save soil report", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteReport(reportId: String): Result<Unit> {
        val uid = currentUserId() ?: return Result.failure(IllegalStateException("Farmer not signed in"))
        return try {
            firestore.collection("users").document(uid).collection("soil_reports").document(reportId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun analyzeSoilImage(imageBytes: ByteArray): Result<SoilReport> {
        // Will be routed through ML backend / API
        val estReport = SoilReport(
            id = UUID.randomUUID().toString(),
            soilCategory = "Fertile Alluvial Loam",
            nitrogen = SoilMeasurement(76f, DataSource.AI_ESTIMATE, confidence = 0.74f),
            phosphorus = SoilMeasurement(32f, DataSource.AI_ESTIMATE, confidence = 0.69f),
            potassium = SoilMeasurement(46f, DataSource.AI_ESTIMATE, confidence = 0.73f),
            ph = SoilMeasurement(6.8f, DataSource.AI_ESTIMATE, confidence = 0.80f),
            moisture = SoilMeasurement(22f, DataSource.AI_ESTIMATE, confidence = 0.70f),
            overallConfidence = 0.73f
        )
        return Result.success(estReport)
    }

    // FARMS & FIELDS
    override fun getFarms(userId: String): Flow<List<Farm>> {
        val uid = userId.ifBlank { currentUserId() ?: "" }
        if (uid.isBlank()) return flowOf(emptyList())

        return firestore.collection("users").document(uid).collection("farms")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    val fields = try {
                        firestore.collection("users").document(uid)
                            .collection("farms").document(doc.id)
                            .collection("fields")
                            .get().await()
                            .documents.mapNotNull { fDoc ->
                                val fData = fDoc.data ?: return@mapNotNull null
                                Field(
                                    id = fDoc.id,
                                    farmId = doc.id,
                                    name = fData["name"] as? String ?: "",
                                    acreage = (fData["acreage"] as? Number)?.toDouble() ?: 0.0,
                                    currentCrop = fData["currentCrop"] as? String
                                )
                            }
                    } catch (_: Exception) {
                        emptyList()
                    }
                    Farm(
                        id = doc.id,
                        userId = uid,
                        name = data["name"] as? String ?: "",
                        state = data["state"] as? String ?: "",
                        district = data["district"] as? String ?: "",
                        village = data["village"] as? String ?: "",
                        totalAcreage = if (fields.isNotEmpty()) fields.sumOf { it.acreage }
                        else (data["totalAcreage"] as? Number)?.toDouble() ?: 0.0,
                        fields = fields
                    )
                }
            }
            .catch { e ->
                Log.e("FirebaseAgriRepo", "Error observing farms: ${e.message}", e)
                emit(emptyList())
            }
    }

    override suspend fun saveFarm(farm: Farm): Result<String> {
        val uid = currentUserId() ?: return Result.failure(IllegalStateException("Farmer must be signed in with Google"))
        val farmId = if (farm.id.isNotBlank()) farm.id else UUID.randomUUID().toString()

        val payload = mapOf(
            "id" to farmId,
            "userId" to uid,
            "name" to farm.name,
            "state" to farm.state,
            "district" to farm.district,
            "village" to farm.village,
            "totalAcreage" to farm.totalAcreage,
            "createdAt" to FieldValue.serverTimestamp()
        )

        return try {
            firestore.collection("users").document(uid).collection("farms").document(farmId).set(payload).await()
            Result.success(farmId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFarm(farmId: String): Result<Unit> {
        val uid = currentUserId() ?: return Result.failure(IllegalStateException("Farmer not signed in"))
        return try {
            firestore.collection("users").document(uid).collection("farms").document(farmId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // FARMER PROFILE
    override fun getFarmerProfile(userId: String): Flow<FarmerProfile?> {
        val uid = userId.ifBlank { currentUserId() ?: "" }
        if (uid.isBlank()) return flowOf(null)

        return firestore.collection("users").document(uid)
            .snapshots()
            .map { doc ->
                if (!doc.exists()) return@map null
                val data = doc.data ?: return@map null
                FarmerProfile(
                    id = uid,
                    name = data["name"] as? String ?: "Farmer",
                    preferredLanguage = data["preferredLanguage"] as? String ?: "en",
                    state = data["state"] as? String ?: "",
                    district = data["district"] as? String ?: "",
                    village = data["village"] as? String ?: "",
                    isOnboarded = true
                )
            }
            .catch { e ->
                Log.e("FirebaseAgriRepo", "Error fetching profile: ${e.message}", e)
                emit(null)
            }
    }

    override suspend fun saveFarmerProfile(profile: FarmerProfile): Result<Unit> {
        val uid = currentUserId() ?: return Result.failure(IllegalStateException("Farmer not signed in"))
        val payload = mapOf(
            "userId" to uid,
            "name" to profile.name,
            "preferredLanguage" to profile.preferredLanguage,
            "state" to profile.state,
            "district" to profile.district,
            "village" to profile.village,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            firestore.collection("users").document(uid).set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveField(farmId: String, field: Field): Result<String> {
        val uid = currentUserId() ?: return Result.failure(IllegalStateException("Farmer not signed in"))
        val fieldId = if (field.id.isNotBlank()) field.id else UUID.randomUUID().toString()
        val payload = mutableMapOf<String, Any>(
            "id" to fieldId,
            "userId" to uid,
            "name" to field.name,
            "acreage" to field.acreage,
            "createdAt" to FieldValue.serverTimestamp()
        )
        if (!field.currentCrop.isNullOrBlank()) {
            payload["currentCrop"] = field.currentCrop
        }

        return try {
            firestore.collection("users").document(uid)
                .collection("farms").document(farmId)
                .collection("fields").document(fieldId)
                .set(payload).await()
            Result.success(fieldId)
        } catch (e: Exception) {
            Log.e("FirebaseAgriRepo", "Failed to save field: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteField(farmId: String, fieldId: String): Result<Unit> {
        val uid = currentUserId() ?: return Result.failure(IllegalStateException("Farmer not signed in"))
        return try {
            firestore.collection("users").document(uid)
                .collection("farms").document(farmId)
                .collection("fields").document(fieldId)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseAgriRepo", "Failed to delete field: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun submitFeedback(isUseful: Boolean, comment: String): Result<String> {
        val uid = currentUserId() ?: return Result.failure(IllegalStateException("Farmer not signed in"))
        val feedbackId = "fb_${System.currentTimeMillis()}"
        val payload = mapOf(
            "id" to feedbackId,
            "userId" to uid,
            "isUseful" to isUseful,
            "comment" to comment,
            "createdAt" to FieldValue.serverTimestamp()
        )

        return try {
            firestore.collection("feedback").document(feedbackId).set(payload).await()
            Result.success(feedbackId)
        } catch (e: Exception) {
            Log.e("FirebaseAgriRepo", "Failed to submit feedback: ${e.message}", e)
            Result.failure(e)
        }
    }
}
