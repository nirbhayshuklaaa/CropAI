package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.TranslationKey
import com.example.core.i18n.stringResource
import com.example.domain.model.DataSource
import com.example.ui.UiState
import com.example.ui.components.LanguagePickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoilAnalysisScreen(
    uiState: UiState,
    onBack: () -> Unit,
    onNavigateHome: () -> Unit = {},
    onAnalyzeBitmap: (Bitmap) -> Unit = {},
    onClearError: () -> Unit = {},
    onAnalyzeManual: (n: Float, p: Float, k: Float, ph: Float, moisture: Float, source: DataSource) -> Unit,
    onViewRecommendations: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit = {}
) {
    val context = LocalContext.current
    val isHindi = uiState.currentLanguage == AppLanguage.HINDI
    var showLanguagePicker by remember { mutableStateOf(false) }

    var selectedModeIndex by remember { mutableIntStateOf(0) }
    var selectedSource by remember { mutableStateOf(DataSource.LAB_TESTED) }

    var nitrogenInput by remember { mutableStateOf("") }
    var phosphorusInput by remember { mutableStateOf("") }
    var potassiumInput by remember { mutableStateOf("") }
    var phInput by remember { mutableStateOf("") }
    var moistureInput by remember { mutableStateOf("") }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            onAnalyzeBitmap(bitmap)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            cameraLauncher.launch(null)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(context.contentResolver, uri)
                    ) { decoder, _, _ -> decoder.isMutableRequired = true }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                if (bitmap != null) {
                    onAnalyzeBitmap(bitmap)
                }
            } catch (e: Exception) {
                Log.e("SoilAnalysisScreen", "Failed to load soil image: ${e.message}")
            }
        }
    }

    if (showLanguagePicker) {
        LanguagePickerDialog(
            currentLanguage = uiState.currentLanguage,
            onLanguageSelected = onLanguageSelected,
            onDismissRequest = { showLanguagePicker = false }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isHindi) "मिट्टी की जांच" else "Soil Testing",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isHindi) "कैमरा फोटो या लैब रिपोर्ट दर्ज करें" else "Photo scan or lab card entry",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("soil_screen_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateHome, modifier = Modifier.testTag("soil_screen_home_button")) {
                        Icon(Icons.Default.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showLanguagePicker = true }, modifier = Modifier.testTag("soil_screen_lang_button")) {
                        Icon(Icons.Default.Language, contentDescription = "Language", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 3-Mode Selector
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedModeIndex == 0,
                    onClick = {
                        selectedModeIndex = 0
                        onClearError()
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                    icon = { Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp)) }
                ) {
                    Text(text = if (isHindi) "कैमरा" else "Camera", fontSize = 12.sp)
                }
                SegmentedButton(
                    selected = selectedModeIndex == 1,
                    onClick = {
                        selectedModeIndex = 1
                        onClearError()
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                    icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp)) }
                ) {
                    Text(text = if (isHindi) "गैलरी" else "Upload", fontSize = 12.sp)
                }
                SegmentedButton(
                    selected = selectedModeIndex == 2,
                    onClick = {
                        selectedModeIndex = 2
                        onClearError()
                        nitrogenInput = ""
                        phosphorusInput = ""
                        potassiumInput = ""
                        phInput = ""
                        moistureInput = ""
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                    icon = { Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp)) }
                ) {
                    Text(text = if (isHindi) "लैब मान" else "Manual Lab", fontSize = 12.sp)
                }
            }

            // STRICT VERIFICATION ERROR BANNER
            val activeError = uiState.soilScanError ?: uiState.errorMessage
            if (activeError != null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("soil_scan_error_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Invalid Soil Image",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isHindi) "सही मिट्टी की तस्वीर नहीं मिली" else "Soil Image Not Detected",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = activeError,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                onClearError()
                                if (selectedModeIndex == 0) {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (hasPermission) {
                                        cameraLauncher.launch(null)
                                    } else {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                } else if (selectedModeIndex == 1) {
                                    galleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isHindi) "फिर से मिट्टी की फोटो लें" else "Capture Correct Soil Photo",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            when (selectedModeIndex) {
                0, 1 -> {
                    val isCameraTab = selectedModeIndex == 0
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = if (isCameraTab) {
                                    if (isHindi) "कैमरे से मिट्टी स्कैन करें" else "Scan Farm Soil with Camera"
                                } else {
                                    if (isHindi) "गैलरी से मिट्टी की फोटो चुनें" else "Upload Soil Photo from Gallery"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isHindi)
                                            "साफ दिन के उजाले में केवल खेत की मिट्टी की नजदीकी फोटो लें। फ्रेम में हाथ, पत्तियां या दीवार न आएं।"
                                        else
                                            "Hold camera close to bare farm topsoil in daylight. Keep hands, walls, leaves, and objects out of frame.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }

                            if (uiState.capturedSoilPhotoBitmap != null && uiState.currentSoilReport != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(170.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                ) {
                                    Image(
                                        bitmap = uiState.capturedSoilPhotoBitmap.asImageBitmap(),
                                        contentDescription = "Verified Soil Sample",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.Black.copy(alpha = 0.65f),
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = uiState.soilVisionClassification ?: "Soil Sample Verified",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    onClearError()
                                    if (isCameraTab) {
                                        val hasPermission = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.CAMERA
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (hasPermission) {
                                            cameraLauncher.launch(null)
                                        } else {
                                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    } else {
                                        galleryLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag(if (isCameraTab) "capture_soil_photo_button" else "upload_soil_photo_button"),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !uiState.isAnalyzing
                            ) {
                                if (uiState.isAnalyzing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (isHindi) "मिट्टी की जांच हो रही है..." else "Checking Soil Sample...")
                                } else {
                                    Icon(
                                        imageVector = if (isCameraTab) Icons.Default.CameraAlt else Icons.Default.PhotoLibrary,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isCameraTab) {
                                            if (isHindi) "कैमरे से मिट्टी की फोटो लें" else stringResource(TranslationKey.TAKE_SOIL_PHOTO)
                                        } else {
                                            if (isHindi) "गैलरी से मिट्टी की फोटो चुनें" else stringResource(TranslationKey.UPLOAD_SOIL_PHOTO)
                                        },
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isHindi) "मृदा स्वास्थ्य कार्ड के मान" else "Soil Health Card Values",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                OutlinedButton(
                                    onClick = {
                                        nitrogenInput = ""
                                        phosphorusInput = ""
                                        potassiumInput = ""
                                        phInput = ""
                                        moistureInput = ""
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("clear_all_inputs_button")
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isHindi) "साफ करें" else "Clear", fontSize = 12.sp)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedSource == DataSource.LAB_TESTED,
                                    onClick = { selectedSource = DataSource.LAB_TESTED },
                                    label = { Text(stringResource(TranslationKey.SOURCE_LAB_TESTED)) },
                                    leadingIcon = if (selectedSource == DataSource.LAB_TESTED) {
                                        { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                                FilterChip(
                                    selected = selectedSource == DataSource.USER_ENTERED,
                                    onClick = { selectedSource = DataSource.USER_ENTERED },
                                    label = { Text(stringResource(TranslationKey.SOURCE_USER_ENTERED)) },
                                    leadingIcon = if (selectedSource == DataSource.USER_ENTERED) {
                                        { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = nitrogenInput,
                                    onValueChange = { nitrogenInput = it },
                                    label = { Text(stringResource(TranslationKey.NITROGEN)) },
                                    placeholder = { Text("e.g. 120") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_nitrogen"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = phosphorusInput,
                                    onValueChange = { phosphorusInput = it },
                                    label = { Text(stringResource(TranslationKey.PHOSPHORUS)) },
                                    placeholder = { Text("e.g. 25") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_phosphorus"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = potassiumInput,
                                    onValueChange = { potassiumInput = it },
                                    label = { Text(stringResource(TranslationKey.POTASSIUM)) },
                                    placeholder = { Text("e.g. 180") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_potassium"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = phInput,
                                    onValueChange = { phInput = it },
                                    label = { Text(stringResource(TranslationKey.PH_LEVEL)) },
                                    placeholder = { Text("e.g. 6.8") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_ph"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            OutlinedTextField(
                                value = moistureInput,
                                onValueChange = { moistureInput = it },
                                label = { Text(stringResource(TranslationKey.MOISTURE)) },
                                placeholder = { Text("e.g. 20%") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_moisture"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            Button(
                                onClick = {
                                    val n = nitrogenInput.toFloatOrNull() ?: 60f
                                    val p = phosphorusInput.toFloatOrNull() ?: 30f
                                    val k = potassiumInput.toFloatOrNull() ?: 40f
                                    val ph = phInput.toFloatOrNull() ?: 6.5f
                                    val m = moistureInput.toFloatOrNull() ?: 20f
                                    onAnalyzeManual(n, p, k, ph, m, selectedSource)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("submit_soil_values_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Science, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isHindi) "जांच रिपोर्ट तैयार करें" else "Analyze & Get Crop Plan",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // SOIL REPORT RESULTS CARD (ONLY SHOWN FOR VALID SOIL)
            uiState.currentSoilReport?.let { report ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isHindi) "मिट्टी स्वास्थ्य रिपोर्ट" else "Soil Health Report",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = report.soilCategory,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(
                                onClick = {
                                    val shareText = "CropAI Soil Health Card\n" +
                                        "Soil: ${report.soilCategory}\n" +
                                        "Nitrogen (N): ${report.nitrogen?.value?.toInt()} kg/ha\n" +
                                        "Phosphorus (P): ${report.phosphorus?.value?.toInt()} kg/ha\n" +
                                        "Potassium (K): ${report.potassium?.value?.toInt()} kg/ha\n" +
                                        "pH: ${report.ph?.value}\n" +
                                        "Moisture: ${report.moisture?.value?.toInt()}%"
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Soil Health Report")
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Soil Report"))
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("share_soil_health_card_button")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share Soil Report", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        // 2x2 Clean Nutrient Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            NutrientTile(
                                label = if (isHindi) "नाइट्रोजन (N)" else "Nitrogen (N)",
                                value = "${report.nitrogen?.value?.toInt() ?: 0} kg/ha",
                                status = report.nitrogen?.interpretation ?: "Normal",
                                modifier = Modifier.weight(1f)
                            )
                            NutrientTile(
                                label = if (isHindi) "फास्फोरस (P)" else "Phosphorus (P)",
                                value = "${report.phosphorus?.value?.toInt() ?: 0} kg/ha",
                                status = report.phosphorus?.interpretation ?: "Normal",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            NutrientTile(
                                label = if (isHindi) "पोटाश (K)" else "Potassium (K)",
                                value = "${report.potassium?.value?.toInt() ?: 0} kg/ha",
                                status = report.potassium?.interpretation ?: "Normal",
                                modifier = Modifier.weight(1f)
                            )
                            NutrientTile(
                                label = if (isHindi) "मिट्टी pH" else "Soil pH",
                                value = "${report.ph?.value ?: 6.8f}",
                                status = report.ph?.interpretation ?: "Neutral",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${if (isHindi) "नमी" else "Moisture"}: ${report.moisture?.value?.toInt() ?: 18}% (${report.moisture?.interpretation ?: "Adequate"})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        if (!uiState.geminiSoilSuggestionsEn.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Spa,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isHindi) "मिट्टी सुधार सलाह" else "Soil Improvement Advice",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (isHindi && !uiState.geminiSoilSuggestionsHi.isNullOrBlank())
                                            uiState.geminiSoilSuggestionsHi
                                        else
                                            uiState.geminiSoilSuggestionsEn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onViewRecommendations,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("view_recommendations_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isHindi) "उपयुक्त फसल और खाद देखें" else "See Recommended Crops & Fertilizer",
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun NutrientTile(
    label: String,
    value: String,
    status: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
        }
    }
}
