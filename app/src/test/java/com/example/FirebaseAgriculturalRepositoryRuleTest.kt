package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.repository.FirebaseAgriculturalRepository
import com.example.domain.model.DataSource
import com.example.domain.model.SoilMeasurement
import com.example.domain.model.SoilReport
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class FirebaseAgriculturalRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun testSaveAndRetrieveSoilReportForAuthenticatedFarmer() = runBlocking {
        val email = "farmer_${UUID.randomUUID().toString().take(8)}@kisan.in"
        val uid = signInTestUser(email)
        val repository = FirebaseAgriculturalRepository(firestore)

        val reportId = "report_${UUID.randomUUID().toString().take(8)}"
        val testReport = SoilReport(
            id = reportId,
            userId = uid,
            soilCategory = "Alluvial Loam",
            nitrogen = SoilMeasurement(78f, DataSource.LAB_TESTED),
            phosphorus = SoilMeasurement(34f, DataSource.LAB_TESTED),
            potassium = SoilMeasurement(48f, DataSource.LAB_TESTED),
            ph = SoilMeasurement(6.8f, DataSource.LAB_TESTED),
            overallConfidence = 0.95f
        )

        val saveResult = repository.saveReport(testReport)
        assertTrue("Save report should succeed", saveResult.isSuccess)

        val retrievedReport = repository.getReportById(reportId)
        assertNotNull("Report should be retrieved from emulator", retrievedReport)
        assertEquals("Alluvial Loam", retrievedReport?.soilCategory)
        assertEquals(78f, retrievedReport?.nitrogen?.value)

        val flowReports = withTimeout(3000L) {
            repository.getSavedReports(uid).first { list -> list.any { it.id == reportId } }
        }
        assertTrue("Stream should emit the saved report", flowReports.any { it.id == reportId })
    }

    @Test
    fun testUnauthenticatedWriteFails() = runBlocking {
        auth.signOut()
        val repository = FirebaseAgriculturalRepository(firestore)

        val unauthReport = SoilReport(
            id = "unauth_report",
            userId = "someone_else",
            soilCategory = "Clay",
            nitrogen = SoilMeasurement(50f, DataSource.USER_ENTERED)
        )

        val result = repository.saveReport(unauthReport)
        assertTrue("Unauthenticated save must fail", result.isFailure)
    }
}
