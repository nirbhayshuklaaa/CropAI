package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.TranslationKey
import com.example.core.i18n.stringResource

import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.core.location.LocationHelper

@Composable
fun OnboardingWizardScreen(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onFinishOnboarding: (name: String, state: String, district: String, village: String, farmName: String, acreage: Double) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var step by remember { mutableIntStateOf(1) }

    var farmerName by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("Uttar Pradesh") }
    var district by remember { mutableStateOf("Varanasi") }
    var village by remember { mutableStateOf("Kashi Rural") }
    var farmName by remember { mutableStateOf("Main Field (मुख्य खेत)") }
    var acreageInput by remember { mutableStateOf("2.5") }
    var gpsStatusMessage by remember { mutableStateOf<String?>(null) }
    var isDetectingLocation by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            coroutineScope.launch {
                isDetectingLocation = true
                gpsStatusMessage = "Detecting exact satellite GPS coordinates..."
                val helper = LocationHelper(context)
                helper.fetchExactLocation()
                    .onSuccess { loc ->
                        isDetectingLocation = false
                        gpsStatusMessage = "✓ Exact Location: ${loc.formattedAddress}"
                        state = loc.state
                        district = loc.district
                        village = loc.village
                    }
                    .onFailure { error ->
                        isDetectingLocation = false
                        gpsStatusMessage = "Could not detect exact GPS: ${error.message ?: "Unknown error"}. Using defaults."
                    }
            }
        } else {
            gpsStatusMessage = "GPS permission denied. Please verify your State and District manually."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Progress indicator (5 Steps)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Step $step of 5",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(TranslationKey.APP_NAME),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { step / 5f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedContent(
            targetState = step,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "onboarding_step_transition"
        ) { currentStep ->
            when (currentStep) {
                1 -> {
                    // STEP 1: Language Selection (6 Indian Languages)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
                        }

                        Text(
                            text = stringResource(TranslationKey.ONBOARDING_STEP_1_TITLE),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Choose your preferred language for advice, reports and voice prompts.\nअपनी भाषा का चयन करें।",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // 6 Indian Language Cards
                        AppLanguage.entries.forEach { lang ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (currentLanguage == lang)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (currentLanguage == lang) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                    else MaterialTheme.colorScheme.outlineVariant
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (currentLanguage == lang) 2.dp else 1.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onLanguageSelected(lang) }
                                    .testTag("onboard_lang_${lang.code}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = lang.nativeName,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (currentLanguage == lang) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = lang.displayName,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (currentLanguage == lang) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { step = 2 },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("onboard_step1_next_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(stringResource(TranslationKey.NEXT_STEP), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }

                2 -> {
                    // STEP 2: Farmer Name & Welcome
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
                        }

                        Text(
                            text = stringResource(TranslationKey.ONBOARDING_STEP_2_TITLE),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "How should CropAI address you in recommendations and soil health reports?",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = farmerName,
                                    onValueChange = { farmerName = it },
                                    label = { Text(stringResource(TranslationKey.NAME_LABEL)) },
                                    placeholder = { Text("e.g. Ramesh Kumar / रमेश कुमार") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("onboard_farmer_name_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { step = 1 },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(TranslationKey.BACK))
                            }
                            Button(
                                onClick = {
                                    if (farmerName.isBlank()) farmerName = "Farmer"
                                    step = 3
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("onboard_step2_next_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(stringResource(TranslationKey.NEXT_STEP), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                3 -> {
                    // STEP 3: Location (State, District, Village) + GPS integration
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
                        }

                        Text(
                            text = stringResource(TranslationKey.ONBOARDING_STEP_3_TITLE),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Crop recommendations use local weather, rainfall, and agro-climatic zones.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // GPS Location Detection Button
                        OutlinedButton(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("detect_gps_location_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(TranslationKey.USE_GPS_LOCATION), fontWeight = FontWeight.SemiBold)
                        }

                        gpsStatusMessage?.let { msg ->
                            Text(
                                text = msg,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = state,
                                    onValueChange = { state = it },
                                    label = { Text(stringResource(TranslationKey.STATE_LABEL)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("onboard_state_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = district,
                                    onValueChange = { district = it },
                                    label = { Text(stringResource(TranslationKey.DISTRICT_LABEL)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("onboard_district_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = village,
                                    onValueChange = { village = it },
                                    label = { Text(stringResource(TranslationKey.VILLAGE_LABEL)) },
                                    placeholder = { Text("e.g. Anandpur") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("onboard_village_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = { step = 2 }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                                Text(stringResource(TranslationKey.BACK))
                            }
                            Button(
                                onClick = { step = 4 },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("onboard_step3_next_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(stringResource(TranslationKey.NEXT_STEP), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                4 -> {
                    // STEP 4: Optional Farm Setup
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Eco, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
                        }

                        Text(
                            text = stringResource(TranslationKey.ONBOARDING_STEP_4_TITLE),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Register your initial plot to track crop rotation and saved soil reports.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = farmName,
                                    onValueChange = { farmName = it },
                                    label = { Text(stringResource(TranslationKey.FARM_NAME_LABEL)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("onboard_farm_name_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = acreageInput,
                                    onValueChange = { acreageInput = it },
                                    label = { Text(stringResource(TranslationKey.ACREAGE_LABEL)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("onboard_acreage_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { step = 5 }, // Skip step
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(TranslationKey.SKIP_STEP))
                            }
                            Button(
                                onClick = { step = 5 },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("onboard_step4_next_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(stringResource(TranslationKey.NEXT_STEP), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                5 -> {
                    // STEP 5: Ready / Completion Summary
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Agriculture, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp))
                        }

                        Text(
                            text = "${stringResource(TranslationKey.WELCOME_BACK)}, $farmerName",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Your personalized agriculture companion is ready with soil analysis, crop prediction, and weather guidance.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Farmer Profile Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                Text("• Name: $farmerName", fontSize = 13.sp)
                                Text("• Language: ${currentLanguage.nativeName} (${currentLanguage.displayName})", fontSize = 13.sp)
                                Text("• Location: $village, $district, $state", fontSize = 13.sp)
                                Text("• Registered Farm: $farmName ($acreageInput Acres)", fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val acre = acreageInput.toDoubleOrNull() ?: 2.0
                                onFinishOnboarding(farmerName, state, district, village, farmName, acre)
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("onboard_launch_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(TranslationKey.COMPLETE_SETUP),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
