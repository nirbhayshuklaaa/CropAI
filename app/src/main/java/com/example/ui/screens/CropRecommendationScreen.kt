package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.core.i18n.TranslationKey
import com.example.core.i18n.stringResource
import com.example.ui.UiState

import com.example.ui.components.VoiceAdvisoryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropRecommendationScreen(
    uiState: UiState,
    onBack: () -> Unit,
    onNavigateHome: () -> Unit = {},
    onNavigateToFertilizer: () -> Unit,
    onNavigateToStore: () -> Unit,
    onSubmitFeedback: (Boolean, String) -> Unit = { _, _ -> },
    onSpeak: (String) -> Unit = {},
    onStopSpeech: () -> Unit = {}
) {
    val prediction = uiState.cropPrediction
    val langCode = uiState.currentLanguage.code
    val context = LocalContext.current
    var feedbackGiven by remember { mutableStateOf<Boolean?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(TranslationKey.RECOMMENDED_CROP_TITLE), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateHome, modifier = Modifier.testTag("crop_screen_home_button")) {
                        Icon(Icons.Default.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.primary)
                    }
                    if (prediction != null) {
                        val crop = prediction.primaryCrop
                        IconButton(
                            onClick = {
                                val shareText = "CropAI Advisory\n" +
                                        "Recommended Crop: ${crop.getLocalizedName(langCode)} (${crop.nameEn})\n" +
                                        "Match Score: ${(prediction.confidence * 100).toInt()}%\n" +
                                        "Season: ${crop.growingSeason}\n" +
                                        "Duration: ${crop.durationDays} Days\n" +
                                        "Rotation Suggestion: ${prediction.rotationSuggestion}\n" +
                                        "Soil Category: ${uiState.currentSoilReport?.soilCategory ?: "Standard Loam"}\n" +
                                        "\nCropAI - Farmer Decision Support"
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Crop Recommendation: ${crop.nameEn}")
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Crop Advisory"))
                            },
                            modifier = Modifier.testTag("share_crop_advisory_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share Advisory")
                        }
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
            Spacer(modifier = Modifier.height(4.dp))

            if (prediction != null) {
                val crop = prediction.primaryCrop

                // Hero Recommendation Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = crop.getLocalizedName(langCode),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${crop.nameEn} • ${crop.scientificName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Grass,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Match Confidence Indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Suitability Match",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${(prediction.confidence * 100).toInt()}% Confidence",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { prediction.confidence },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Season: ${crop.growingSeason} • Duration: ${crop.durationDays} days",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Water Requirement: ${crop.waterRequirement}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                // VOICE AUDIO READOUT
                VoiceAdvisoryButton(
                    textToSpeak = if (uiState.currentLanguage == com.example.core.i18n.AppLanguage.HINDI) {
                        "आपकी मिट्टी के लिए अनुशंसित फसल ${crop.getLocalizedName(langCode)} है। उपयुक्तता स्कोर ${(prediction.confidence * 100).toInt()} प्रतिशत है। बुवाई का मौसम ${crop.growingSeason} है।"
                    } else {
                        "Recommended crop for your soil test is ${crop.nameEn} with ${(prediction.confidence * 100).toInt()} percent suitability score. Growing season is ${crop.growingSeason}."
                    },
                    isSpeaking = uiState.isSpeaking,
                    onSpeak = onSpeak,
                    onStop = onStopSpeech,
                    modifier = Modifier.fillMaxWidth()
                )

                // EXPLAINABILITY: Why are we recommending this?
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(TranslationKey.WHY_THIS_RECOMMENDATION),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        prediction.contributingFactors.forEach { factor ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = factor.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = factor.detail,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // CROP ROTATION SUGGESTION
                prediction.rotationSuggestion?.let { rotation ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Loop,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = stringResource(TranslationKey.CROP_ROTATION_SUGGESTION),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = rotation,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                // ALTERNATIVE CROPS
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(TranslationKey.ALTERNATIVE_CROPS),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        prediction.alternativeCrops.forEach { (altCrop, altConf) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${altCrop.getLocalizedName(langCode)} (${altCrop.nameEn})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${(altConf * 100).toInt()}% Match",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // FARMER FEEDBACK CARD
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (feedbackGiven == null) "Was this crop recommendation helpful?" else "✓ Thank you for your feedback!",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (feedbackGiven == null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Button(
                                    onClick = {
                                        feedbackGiven = true
                                        onSubmitFeedback(true, "Helpful crop recommendation for ${prediction.primaryCrop.nameEn}")
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.testTag("feedback_thumbs_up_button")
                                ) {
                                    Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Yes, Helpful", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        feedbackGiven = false
                                        onSubmitFeedback(false, "Crop recommendation needs refinement")
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.testTag("feedback_thumbs_down_button")
                                ) {
                                    Icon(Icons.Default.ThumbDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Not Accurate", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // ACTION BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateToFertilizer,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("goto_fertilizer_recommendation_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Spa, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Fertilizer Advice")
                    }

                    OutlinedButton(
                        onClick = onNavigateToStore,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("goto_product_store_seeds_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buy Seeds")
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Please run soil analysis first to generate crop recommendations.")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
