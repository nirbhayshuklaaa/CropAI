package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.MainViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FarmPlotManagementTest {

    private lateinit var app: Application

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        if (FirebaseApp.getApps(app).isEmpty()) {
            FirebaseApp.initializeApp(
                app,
                FirebaseOptions.Builder()
                    .setApplicationId("1:123456:android:abcdef")
                    .setProjectId("test-project")
                    .setApiKey("test-api-key")
                    .build()
            )
        }
    }

    @Test
    fun `addField and deleteField update farms state and total acreage`() {
        val viewModel = MainViewModel(app)

        val initialFields = viewModel.uiState.value.farms.flatMap { it.fields }
        val initialCount = initialFields.size
        assertTrue("Default farm should have initial plots", initialCount >= 2)

        // Add a new plot
        viewModel.addField("River Side Plot", 3.5, "Sugarcane")
        val afterAdd = viewModel.uiState.value.farms.flatMap { it.fields }
        assertEquals(initialCount + 1, afterAdd.size)

        val addedPlot = afterAdd.first { it.name == "River Side Plot" }
        assertEquals(3.5, addedPlot.acreage, 0.01)

        // Delete the added plot
        viewModel.deleteField(addedPlot.id)
        val afterDelete = viewModel.uiState.value.farms.flatMap { it.fields }
        assertEquals(initialCount, afterDelete.size)
        assertFalse(afterDelete.any { it.id == addedPlot.id })
    }
}
