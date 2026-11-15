package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.OutlinedButton
import com.example.core.i18n.TranslationKey
import com.example.core.i18n.stringResource
import com.example.ui.UiState
import com.example.ui.components.VoiceAdvisoryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertilizerRecommendationScreen(
    uiState: UiState,
    onBack: () -> Unit,
    onNavigateToStore: () -> Unit,
    onNavigateHome: () -> Unit = {},
    onSubmitFeedback: (Boolean, String) -> Unit = { _, _ -> },
    onSpeak: (String) -> Unit = {},
    onStopSpeech: () -> Unit = {}
) {
    val result = uiState.fertilizerRecommendation
    val context = LocalContext.current
    var feedbackGiven by remember { mutableStateOf<Boolean?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(TranslationKey.RECOMMENDED_FERTILIZER_TITLE), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateHome, modifier = Modifier.testTag("fert_screen_home_button")) {
                        Icon(Icons.Default.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.primary)
                    }
                    if (result != null) {
                        val fert = result.recommendedFertilizer
                        IconButton(
                            onClick = {
                                val shareText = "CropAI Fertilizer Advisory\n" +
                                        "Recommended: ${fert.nameEn} (${fert.nameHi})\n" +
                                        "Nutrient Ratio: ${fert.nutrientComposition}\n" +
                                        "Application Advice: ${result.applicationAdvice}\n" +
                                        "Standard Guideline: ${fert.standardGuideline}\n" +
                                        "Reason: ${result.deficiencyReason}\n" +
                                        "Safety Precaution: ${result.safetyNote}\n" +
                                        "\nCropAI - Farmer Decision Support"
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Fertilizer Advisory: ${fert.nameEn}")
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Fertilizer Advisory"))
                            },
                            modifier = Modifier.testTag("share_fertilizer_advisory_button")
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

            if (result != null) {
                val fert = result.recommendedFertilizer

                // Fertilizer Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
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
                                    text = fert.nameEn,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = fert.nameHi,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Nutrient Grade: ${fert.nutrientComposition}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(MaterialTheme.colorScheme.secondary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Spa,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Deficiency Diagnosed: ${result.deficiencyReason}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // VOICE AUDIO READOUT
                VoiceAdvisoryButton(
                    textToSpeak = if (uiState.currentLanguage == com.example.core.i18n.AppLanguage.HINDI) {
                        "उर्वरक सलाह: आपकी फसल के लिए अनुशंसित खाद ${fert.nameHi} है। ग्रेड ${fert.nutrientComposition}। कमी का कारण: ${result.deficiencyReason}। प्रयोग विधि: ${result.applicationAdvice}।"
                    } else {
                        "Fertilizer Advisory: Recommended fertilizer is ${fert.nameEn}. Nutrient grade ${fert.nutrientComposition}. Deficiency diagnosed: ${result.deficiencyReason}. Application advice: ${result.applicationAdvice}."
                    },
                    isSpeaking = uiState.isSpeaking,
                    onSpeak = onSpeak,
                    onStop = onStopSpeech,
                    modifier = Modifier.fillMaxWidth()
                )

                // Application Guidance
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
                                text = stringResource(TranslationKey.APPLICATION_GUIDELINES),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = result.applicationAdvice,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                // CRITICAL FERTILIZER SAFETY NOTE
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Safety & Environmental Advisory",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${stringResource(TranslationKey.FERTILIZER_SAFETY_NOTE)}\n${fert.safetyWarning}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                // BIO & ORGANIC ALTERNATIVES
                if (result.organicAlternatives.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Organic & Biological Alternatives",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            result.organicAlternatives.forEach { alt ->
                                Text(
                                    text = "• ${alt.nameEn} (${alt.nameHi}) - ${alt.nutrientComposition}",
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
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
                            text = if (feedbackGiven == null) "Was this fertilizer prescription helpful?" else "✓ Thank you for your feedback!",
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
                                        onSubmitFeedback(true, "Helpful fertilizer recommendation for ${result.recommendedFertilizer.nameEn}")
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.testTag("fertilizer_feedback_thumbs_up_button")
                                ) {
                                    Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Yes, Helpful", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        feedbackGiven = false
                                        onSubmitFeedback(false, "Fertilizer prescription dosage needs refinement")
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.testTag("fertilizer_feedback_thumbs_down_button")
                                ) {
                                    Icon(Icons.Default.ThumbDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Needs Review", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // ACTION: BUY ON FLIPKART / GO TO STORE
                Button(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.flipkart.com/search?q=fertilizer")
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("buy_fertilizer_flipkart_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(TranslationKey.BUY_ON_FLIPKART),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            } else {
                Text("Please analyze soil first to calculate nutrient balance.")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
