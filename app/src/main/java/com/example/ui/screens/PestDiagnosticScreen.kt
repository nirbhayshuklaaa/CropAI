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
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.domain.model.DiseaseSeverity
import com.example.ui.UiState
import com.example.ui.components.VoiceAdvisoryButton

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PestDiagnosticScreen(
    uiState: UiState,
    onBack: () -> Unit,
    onNavigateHome: () -> Unit = {},
    onSelectCrop: (String) -> Unit,
    onToggleSymptom: (String) -> Unit,
    onClearSymptoms: () -> Unit,
    onClearError: () -> Unit = {},
    onSetPhotoUri: (String?) -> Unit,
    onDiagnoseLeafBitmap: (Bitmap) -> Unit = {},
    onRunDiagnosis: () -> Unit,
    onSpeak: (String) -> Unit,
    onStopSpeech: () -> Unit
) {
    val context = LocalContext.current
    val isHindi = uiState.currentLanguage == AppLanguage.HINDI
    var selectedTab by remember { mutableIntStateOf(0) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            onDiagnoseLeafBitmap(bitmap)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            cameraLauncher.launch(null)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        onSetPhotoUri(uri?.toString())
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
                    onDiagnoseLeafBitmap(bitmap)
                }
            } catch (e: Exception) {
                Log.e("PestDiagnosticScreen", "Error loading leaf image: ${e.message}")
            }
        }
    }

    val availableCrops = listOf("Wheat", "Rice", "Tomato", "Potato", "Cotton", "Maize", "Mustard")
    val currentCrop = uiState.selectedDiagnosticCrop

    val cropSymptoms = when (currentCrop.lowercase()) {
        "wheat" -> listOf(
            "Yellow-orange powdery stripes on leaves",
            "Yellow dust on fingers when touched",
            "Tiny green/black aphids on earhead",
            "Sticky honeydew with black mold"
        )
        "rice" -> listOf(
            "Spindle/eye shaped gray lesions on leaves",
            "Black rot at neck node of panicle",
            "Circular patches of drying plants ('Hopper burn')",
            "Tiny brown insects clustered at stem base"
        )
        "tomato" -> listOf(
            "Concentric rings / bullseye spots on older leaves",
            "Yellow halo around circular brown leaf lesions",
            "Lower leaves dying and falling off",
            "Sunken dark leathery rot at fruit stem end"
        )
        "potato" -> listOf(
            "Water-soaked dark lesions on leaf tips and margins",
            "White cottony fungal growth on leaf undersides",
            "Foul odor with rapid foliage blackening",
            "Brown dry rot beneath potato skin"
        )
        "cotton" -> listOf(
            "Rosetted flowers that fail to open ('Rosette stage')",
            "Tiny bore holes in green bolls with frass",
            "Premature dropping of squares and bolls",
            "Yellow stained lint inside boll"
        )
        "maize" -> listOf(
            "Ragged, torn shot-holes on whorl leaves",
            "Sawdust-like frass inside central plant whorl",
            "Larva has inverted Y on head",
            "Damage to corn ear tips and silk"
        )
        "mustard" -> listOf(
            "Colonies of tiny yellow-green aphids sucking flowers",
            "Curling and distortion of flowering twigs",
            "Pods fail to form healthy seeds",
            "Black sticky residue on stems"
        )
        else -> listOf("Leaf spots", "Yellowing", "Insect holes", "Wilting")
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isHindi) "पत्ती रोग जांच" else "Crop Leaf Doctor",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isHindi) "रोग पहचान एवं जैविक-रासायनिक उपचार" else "Identify leaf diseases & remedies",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("pest_screen_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateHome,
                        modifier = Modifier.testTag("pest_screen_home_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onClearSymptoms,
                        modifier = Modifier.testTag("pest_screen_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Form",
                            tint = MaterialTheme.colorScheme.primary
                        )
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
            // STRICT LEAF VALIDATION ERROR BANNER
            val activeError = uiState.leafScanError ?: uiState.errorMessage
            if (activeError != null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("leaf_scan_error_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Invalid Leaf Photo",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isHindi) "सही पत्ती की तस्वीर नहीं मिली" else "Leaf Image Not Detected",
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
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPermission) {
                                    cameraLauncher.launch(null)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isHindi) "फिर से पत्ती की फोटो लें" else "Capture Correct Leaf Photo",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // STEP 1: SELECT CROP
            Text(
                text = if (isHindi) "1. अपनी फसल चुनें" else "1. Select Crop",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableCrops.forEach { crop ->
                    val isSelected = crop.equals(currentCrop, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCrop(crop) },
                        label = {
                            val hindiName = when (crop) {
                                "Wheat" -> "गेहूं"
                                "Rice" -> "धान"
                                "Tomato" -> "टमाटर"
                                "Potato" -> "आलू"
                                "Cotton" -> "कपास"
                                "Maize" -> "मक्का"
                                "Mustard" -> "सरसों"
                                else -> crop
                            }
                            Text(if (isHindi) "$hindiName ($crop)" else crop)
                        },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("crop_chip_${crop.lowercase()}")
                    )
                }
            }

            // STEP 2: SCAN LEAF PHOTO
            Text(
                text = if (isHindi) "2. पत्ती की फोटो स्कैन करें" else "2. Scan Affected Leaf",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (uiState.capturedLeafPhotoBitmap != null && uiState.diagnosticResult != null)
                                    (if (isHindi) "पत्ती की तस्वीर प्राप्त हुई" else "Leaf Photo Ready")
                                else
                                    (if (isHindi) "रोगग्रस्त पत्ती की साफ फोटो लें" else "Capture or Upload Plant Leaf"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isHindi) "केवल पौधे की पत्ती पर कैमरा फोकस करें" else "Focus clearly on the affected plant leaf",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHindi)
                                    "सुनिश्चित करें कि फोटो में पत्ती साफ दिखाई दे। किसी अन्य वस्तु की फोटो अमान्य होगी।"
                                else
                                    "Make sure the plant leaf fills the frame. Non-leaf photos will be rejected.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    if (uiState.capturedLeafPhotoBitmap != null && uiState.diagnosticResult != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            Image(
                                bitmap = uiState.capturedLeafPhotoBitmap.asImageBitmap(),
                                contentDescription = "Scanned Leaf Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onClearError()
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPermission) {
                                    cameraLauncher.launch(null)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            enabled = !uiState.isDiagnosing,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("camera_leaf_photo_button")
                        ) {
                            if (uiState.isDiagnosing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isHindi) "कैमरा" else "Camera", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                onClearError()
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = !uiState.isDiagnosing,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("pick_leaf_photo_button")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isHindi) "गैलरी" else "Gallery", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // STEP 3: SYMPTOMS SELECTION
            Text(
                text = if (isHindi) "3. या दिखने वाले लक्षण चुनें" else "3. Or Select Visible Symptoms",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                cropSymptoms.forEach { symptom ->
                    val isChecked = uiState.selectedDiagnosticSymptoms.contains(symptom)
                    FilterChip(
                        selected = isChecked,
                        onClick = { onToggleSymptom(symptom) },
                        label = {
                            Text(
                                text = symptom,
                                fontSize = 12.sp,
                                maxLines = 2
                            )
                        },
                        leadingIcon = if (isChecked) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.testTag("symptom_chip_${symptom.take(15).lowercase().replace(" ", "_")}")
                    )
                }
            }

            Button(
                onClick = onRunDiagnosis,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("run_diagnosis_button")
            ) {
                Icon(Icons.Default.Healing, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isHindi) "रोग पहचान व उपचार देखें" else "Check Disease & Remedies",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }

            // DIAGNOSIS RESULT CARD
            uiState.diagnosticResult?.let { result ->
                val disease = result.matchedDisease
                val displayDiseaseName = if (isHindi) disease.diseaseNameHindi else disease.diseaseName
                val speechContent = if (isHindi) {
                    "फसल रोग पहचान: ${disease.diseaseNameHindi}। गंभीरता: ${disease.severity.labelHindi}। जैविक उपचार: ${disease.organicRemedyHindi}। रासायनिक दवा: ${disease.chemicalRemedyHindi}।"
                } else {
                    "Diagnostic Advisory for ${disease.cropName}. Detected: ${disease.diseaseName}. Severity is ${disease.severity.label}. Organic remedy: ${disease.organicRemedy}. Chemical control: ${disease.chemicalRemedy}."
                }

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.5.dp,
                            color = when (disease.severity) {
                                DiseaseSeverity.HIGH -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                DiseaseSeverity.MEDIUM -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                DiseaseSeverity.LOW -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            },
                            shape = RoundedCornerShape(18.dp)
                        )
                        .testTag("diagnosis_result_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isHindi) "रोग पहचान परिणाम" else "Diagnosis Result",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = displayDiseaseName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${disease.pathogenType} • ${disease.cropName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            val severityBg = when (disease.severity) {
                                DiseaseSeverity.HIGH -> MaterialTheme.colorScheme.errorContainer
                                DiseaseSeverity.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
                                DiseaseSeverity.LOW -> MaterialTheme.colorScheme.primaryContainer
                            }
                            val severityTextColor = when (disease.severity) {
                                DiseaseSeverity.HIGH -> MaterialTheme.colorScheme.onErrorContainer
                                DiseaseSeverity.MEDIUM -> MaterialTheme.colorScheme.onSecondaryContainer
                                DiseaseSeverity.LOW -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = severityBg
                            ) {
                                Text(
                                    text = if (isHindi) disease.severity.labelHindi else disease.severity.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = severityTextColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isHindi) "मिलान स्कोर: ${result.confidencePercentage}%" else "Match Score: ${result.confidencePercentage}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { result.confidencePercentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        VoiceAdvisoryButton(
                            textToSpeak = speechContent,
                            isSpeaking = uiState.isSpeaking,
                            onSpeak = onSpeak,
                            onStop = onStopSpeech,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text(if (isHindi) "जैविक उपचार" else "Organic", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = { Text(if (isHindi) "रासायनिक दवा" else "Chemical", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                            )
                            Tab(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                text = { Text(if (isHindi) "बचाव" else "Prevention", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        when (selectedTab) {
                            0 -> {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Eco,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = stringResource(TranslationKey.ORGANIC_REMEDIES),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (isHindi) disease.organicRemedyHindi else disease.organicRemedy,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                            1 -> {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = stringResource(TranslationKey.CHEMICAL_TREATMENT),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (isHindi) disease.chemicalRemedyHindi else disease.chemicalRemedy,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                            2 -> {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Security,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = stringResource(TranslationKey.PREVENTION_TIPS),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val tips = if (isHindi) disease.preventiveMeasuresHindi else disease.preventiveMeasures
                                        tips.forEach { tip ->
                                            Row(
                                                modifier = Modifier.padding(vertical = 2.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Text("• ", fontWeight = FontWeight.Bold)
                                                Text(tip, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (!uiState.geminiLeafVisualNotes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = uiState.geminiLeafVisualNotes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = disease.optimalSprayingWeather,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "CropAI Leaf Diagnosis: $displayDiseaseName")
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        """
                                        CropAI Plant Health Report
                                        Crop: ${disease.cropName}
                                        Diagnosis: $displayDiseaseName
                                        Severity: ${disease.severity.label}

                                        Organic Remedy:
                                        ${if (isHindi) disease.organicRemedyHindi else disease.organicRemedy}

                                        Chemical Treatment:
                                        ${if (isHindi) disease.chemicalRemedyHindi else disease.chemicalRemedy}
                                        """.trimIndent()
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Advisory"))
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("share_diagnosis_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHindi) "रिपोर्ट शेयर करें" else "Share Treatment Plan",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
