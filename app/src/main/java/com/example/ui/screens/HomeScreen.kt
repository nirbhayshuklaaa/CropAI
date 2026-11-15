package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.TranslationKey
import com.example.core.i18n.stringResource
import com.example.domain.model.WeatherCondition
import com.example.ui.UiState
import com.example.ui.components.LanguagePickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: UiState,
    onLanguageSelected: (AppLanguage) -> Unit,
    onSignInWithGoogle: () -> Unit,
    onSignOut: () -> Unit,
    onNavigateToSoilAnalysis: () -> Unit,
    onNavigateToCropRecommendation: () -> Unit,
    onNavigateToFertilizerRecommendation: () -> Unit,
    onNavigateToProductStore: () -> Unit,
    onNavigateToKnowledgeCenter: () -> Unit,
    onNavigateToFarmManagement: () -> Unit,
    onNavigateToPestDiagnostic: () -> Unit = {},
    onNavigateToProfitabilityCalculator: () -> Unit = {},
    onDetectExactLocation: () -> Unit = {},
    onSpeak: (String) -> Unit = {},
    onStopSpeech: () -> Unit = {}
) {
    val context = LocalContext.current
    val isHindi = uiState.currentLanguage == AppLanguage.HINDI

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fine || coarse) {
            onDetectExactLocation()
        }
    }

    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            onDetectExactLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    var showLanguagePicker by remember { mutableStateOf(false) }

    val currentHour = remember {
        java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    }
    val timeGreetingEn = remember(currentHour) {
        when (currentHour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Agriculture,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(TranslationKey.APP_NAME),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isHindi) "किसान फसल सलाहकार" else "Farmer Companion",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    OutlinedButton(
                        onClick = { showLanguagePicker = true },
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("language_picker_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Change Language",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = uiState.currentLanguage.nativeName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. GREETING FIELD HERO BANNER
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(172.dp)
                    .testTag("farmer_hero_banner")
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_farmer_hero),
                        contentDescription = "Indian Farmland",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x28000000),
                                        Color(0x880B2916),
                                        Color(0xEB072111)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Color(0xFFA7F3D0),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${uiState.farmerProfile.district}, ${uiState.farmerProfile.state}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    val intro = when (uiState.currentLanguage) {
                                        AppLanguage.HINDI ->
                                            "नमस्ते ${uiState.farmerProfile.name} जी। अपनी मिट्टी या फसल की पत्ती जांचने के लिए नीचे दिए गए स्कैन बटन दबाएं।"
                                        AppLanguage.ENGLISH ->
                                            "$timeGreetingEn ${uiState.farmerProfile.name}. Tap Scan Soil or Scan Leaf below to inspect your field."
                                        AppLanguage.BENGALI ->
                                            "নমস্কার ${uiState.farmerProfile.name}। আপনার মাটি বা পাতা পরীক্ষা করতে নিচের বাটনে চাপ দিন।"
                                        AppLanguage.MARATHI ->
                                            "नमस्कार ${uiState.farmerProfile.name} जी। आपली माती किंवा पान तपासण्यासाठी खालील बटण दाबा."
                                        AppLanguage.TELUGU ->
                                            "నమస్కారం ${uiState.farmerProfile.name} గారు. మీ నేల లేదా ఆకును పరీక్షించడానికి క్రింది బటన్‌ను నొక్కండి."
                                        AppLanguage.TAMIL ->
                                            "வணக்கம் ${uiState.farmerProfile.name}. உங்கள் மண் அல்லது இலையை பரிசோதிக்க கீழே உள்ள பொத்தானை அழுத்தவும்."
                                    }
                                    onSpeak(intro)
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Listen Greeting",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column {
                            val greetingTitle = when (uiState.currentLanguage) {
                                AppLanguage.HINDI -> "नमस्ते, ${uiState.farmerProfile.name} जी"
                                AppLanguage.ENGLISH -> "$timeGreetingEn, ${uiState.farmerProfile.name}"
                                AppLanguage.BENGALI -> "নমস্কার, ${uiState.farmerProfile.name}"
                                AppLanguage.MARATHI -> "नमस्कार, ${uiState.farmerProfile.name} जी"
                                AppLanguage.TELUGU -> "నమస్కారం, ${uiState.farmerProfile.name} గారు"
                                AppLanguage.TAMIL -> "வணக்கம், ${uiState.farmerProfile.name}"
                            }
                            Text(
                                text = greetingTitle,
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isHindi)
                                    "सही जांच, सही खाद और बेहतर पैदावार"
                                else
                                    "Simple field checks for better crop yield",
                                color = Color(0xFFD1FAE5),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // 2. PRIMARY QUICK FIELD SCAN (2 BIG, EASY-TO-TAP TILES)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isHindi) "तुरंत जांच करें" else "Quick Field Scan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // SCAN SOIL CARD
                    Card(
                        onClick = onNavigateToSoilAnalysis,
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(142.dp)
                            .testTag("start_soil_analysis_button")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.tertiary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Scan Soil",
                                    tint = MaterialTheme.colorScheme.onTertiary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isHindi) "मिट्टी जांचें" else "Scan Soil",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = if (isHindi) "फोटो से N-P-K व pH" else "Check N-P-K & pH",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // SCAN LEAF CARD
                    Card(
                        onClick = onNavigateToPestDiagnostic,
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(142.dp)
                            .testTag("quick_leaf_scan_button")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Healing,
                                    contentDescription = "Scan Leaf",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isHindi) "पत्ती जांचें" else "Scan Leaf",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = if (isHindi) "रोग व कीट का इलाज" else "Spot pests & remedies",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // 3. COMPACT WEATHER STRIP
            uiState.weather?.let { weather ->
                WeatherSummaryCard(
                    weather = weather,
                    isHindi = isHindi,
                    isDetecting = uiState.isFetchingLocation,
                    onDetectLocation = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )
            }

            // 4. FARMING TOOLS (CLEAN 2x2 GRID)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isHindi) "कृषि सेवाएं" else "Farm Services",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ServiceTile(
                        title = stringResource(TranslationKey.RECOMMEND_CROP),
                        subtitle = if (isHindi) "मिट्टी के अनुसार फसल" else "Best crop match",
                        icon = Icons.Default.Grass,
                        iconBg = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_crop_recommendation"),
                        onClick = onNavigateToCropRecommendation
                    )
                    ServiceTile(
                        title = stringResource(TranslationKey.RECOMMEND_FERTILIZER),
                        subtitle = if (isHindi) "संतुलित खाद मात्रा" else "Dosage & timing",
                        icon = Icons.Default.Spa,
                        iconBg = MaterialTheme.colorScheme.secondaryContainer,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_fertilizer_recommendation"),
                        onClick = onNavigateToFertilizerRecommendation
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ServiceTile(
                        title = stringResource(TranslationKey.PROFIT_CALCULATOR_TITLE),
                        subtitle = if (isHindi) "लागत व मुनाफा हिसाब" else "Cost & ROI estimate",
                        icon = Icons.Default.Calculate,
                        iconBg = MaterialTheme.colorScheme.secondaryContainer,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_profit_calculator"),
                        onClick = onNavigateToProfitabilityCalculator
                    )
                    ServiceTile(
                        title = stringResource(TranslationKey.MY_FARM),
                        subtitle = if (isHindi) "खेत व रकबा प्रबंधन" else "Fields & acreage",
                        icon = Icons.Default.Eco,
                        iconBg = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_my_farm"),
                        onClick = onNavigateToFarmManagement
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ServiceTile(
                        title = stringResource(TranslationKey.AGRICULTURAL_PRODUCTS),
                        subtitle = if (isHindi) "बीज व खाद खरीदें" else "Seeds & inputs",
                        icon = Icons.Default.ShoppingBag,
                        iconBg = MaterialTheme.colorScheme.tertiaryContainer,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_product_store"),
                        onClick = onNavigateToProductStore
                    )
                    ServiceTile(
                        title = stringResource(TranslationKey.FARMING_GUIDE),
                        subtitle = if (isHindi) "सरकारी योजना व नियम" else "Guides & schemes",
                        icon = Icons.Default.Book,
                        iconBg = MaterialTheme.colorScheme.surfaceVariant,
                        iconTint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_knowledge_center"),
                        onClick = onNavigateToKnowledgeCenter
                    )
                }
            }

            // 5. RECENT SOIL REPORTS
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(TranslationKey.RECENT_REPORTS),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.savedReports.isNotEmpty()) {
                            Text(
                                text = "${uiState.savedReports.size} saved",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (uiState.savedReports.isEmpty()) {
                        Text(
                            text = stringResource(TranslationKey.NO_REPORTS_YET),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        uiState.savedReports.take(3).forEach { report ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = report.soilCategory,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "N: ${report.nitrogen?.value?.toInt() ?: 0}  •  P: ${report.phosphorus?.value?.toInt() ?: 0}  •  K: ${report.potassium?.value?.toInt() ?: 0}  •  pH ${report.ph?.value ?: 7.0f}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. COMPACT ACCOUNT BACKUP STRIP (CLEAN, NON-INTRUSIVE)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (uiState.firebaseUser != null)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (uiState.firebaseUser != null) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (uiState.firebaseUser != null)
                                        (uiState.firebaseUser.displayName ?: "Farmer Account")
                                    else
                                        (if (isHindi) "रिपोर्ट सुरक्षित रखें" else "Save Reports Online"),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (uiState.firebaseUser != null)
                                        (uiState.firebaseUser.email ?: "Synced to Google account")
                                    else
                                        (if (isHindi) "दूसरे फोन पर भी डेटा पाएं" else "Back up soil & farm history"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (uiState.firebaseUser != null) {
                            TextButton(
                                onClick = onSignOut,
                                modifier = Modifier.testTag("sign_out_button")
                            ) {
                                Text(if (isHindi) "लॉग आउट" else "Sign Out", fontSize = 12.sp)
                            }
                        } else {
                            Button(
                                onClick = onSignInWithGoogle,
                                enabled = !uiState.isSigningIn,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("google_sign_in_button")
                            ) {
                                if (uiState.isSigningIn) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = if (isHindi) "लॉग इन" else "Sign In",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    uiState.syncSuccessMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    uiState.errorMessage?.let { err ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = err,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Text(
                text = stringResource(TranslationKey.DECISION_SUPPORT_DISCLAIMER),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun WeatherSummaryCard(
    weather: WeatherCondition,
    isHindi: Boolean = false,
    isDetecting: Boolean = false,
    onDetectLocation: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = weather.locationName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${weather.conditionDescription} • ${if (isHindi) "नमी" else "Humidity"} ${weather.humidityPercent.toInt()}% • ${if (isHindi) "वर्षा" else "Rain"} ${weather.rainfallMm} mm",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${weather.temperatureCelsius.toInt()}°C",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = onDetectLocation,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("refresh_exact_location_button")
                    ) {
                        if (isDetecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Refresh GPS Weather",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = weather.agriculturalAdvisory,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.height(106.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
