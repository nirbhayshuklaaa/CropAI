package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.TranslationKey
import com.example.core.i18n.stringResource
import com.example.domain.model.CropCostBreakdown
import com.example.ui.UiState
import com.example.ui.components.VoiceAdvisoryButton
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfitabilityCalculatorScreen(
    uiState: UiState,
    onBack: () -> Unit,
    onNavigateHome: () -> Unit = {},
    onSelectCrop: (String) -> Unit,
    onUpdateAcreage: (Double) -> Unit,
    onUpdateParameters: (CropCostBreakdown?, Double?, Double?) -> Unit,
    onSpeak: (String) -> Unit,
    onStopSpeech: () -> Unit
) {
    val context = LocalContext.current
    val isHindi = uiState.currentLanguage == AppLanguage.HINDI
    val numberFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    var showCostEditor by remember { mutableStateOf(false) }

    val crops = listOf("Wheat", "Rice", "Cotton", "Tomato", "Potato", "Mustard", "Maize")
    val selectedCrop = uiState.selectedProfitCrop
    val currentResult = uiState.profitCalculationResult

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(TranslationKey.PROFIT_CALCULATOR_TITLE),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (isHindi) "लागत, उपज व शुद्ध आय का हिसाब" else "Economics, Yield & ROI Estimator",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("profit_screen_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateHome, modifier = Modifier.testTag("profit_screen_home_button")) {
                        Icon(Icons.Default.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.primary)
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

            // STEP 1: CROP SELECTION
            Text(
                text = if (isHindi) "1. फसल का चयन करें / Choose Crop" else "1. Select Target Crop",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                crops.forEach { crop ->
                    val isSelected = crop.equals(selectedCrop, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCrop(crop) },
                        label = {
                            val hindiName = when (crop) {
                                "Wheat" -> "गेहूं"
                                "Rice" -> "धान"
                                "Cotton" -> "कपास"
                                "Tomato" -> "टमाटर"
                                "Potato" -> "आलू"
                                "Mustard" -> "सरसों"
                                "Maize" -> "मक्का"
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
                        modifier = Modifier.testTag("profit_crop_${crop.lowercase()}")
                    )
                }
            }

            // STEP 2: ACREAGE STEPPER
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isHindi) "खेती का रकबा (एकड़)" else "Cultivated Area (Acres)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Selected: ${uiState.profitAcreage} Acre(s)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val current = uiState.profitAcreage
                                    if (current > 0.5) onUpdateAcreage(current - 0.5)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                    .testTag("acreage_decrease_button")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease Acres", modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${uiState.profitAcreage}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(
                                onClick = {
                                    val current = uiState.profitAcreage
                                    if (current < 50.0) onUpdateAcreage(current + 0.5)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                    .testTag("acreage_increase_button")
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Increase Acres",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Slider(
                        value = uiState.profitAcreage.toFloat(),
                        onValueChange = { onUpdateAcreage(it.toDouble()) },
                        valueRange = 0.5f..20f,
                        steps = 39,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // STEP 3: FINANCIAL REVENUE & PROFIT KPI CARD
            currentResult?.let { result ->
                val isProfit = result.netProfit >= 0
                val profitColor = if (isProfit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

                val speechText = if (isHindi) {
                    "फसल ${result.cropName} के लिए ${result.acreage} एकड़ का वित्तीय विश्लेषण। कुल खेती लागत लगभग ₹${result.totalCost.toInt()} है। अनुमानित कुल उपज ${result.expectedTotalYieldQuintals.toInt()} क्विंटल होगी। शुद्ध मुनाफा ₹${result.netProfit.toInt()} रहेगा, जो प्रति एकड़ ₹${result.netProfitPerAcre.toInt()} है। निवेश पर लाभ ${result.returnOnInvestmentPercent.toInt()} प्रतिशत है।"
                } else {
                    "Economic projection for ${result.cropName} across ${result.acreage} acres. Total cost of cultivation is ₹${result.totalCost.toInt()}. Expected harvest yield is ${result.expectedTotalYieldQuintals.toInt()} quintals. Projected net profit is ₹${result.netProfit.toInt()}, which is ₹${result.netProfitPerAcre.toInt()} per acre, giving an ROI of ${result.returnOnInvestmentPercent.toInt()} percent."
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, profitColor.copy(alpha = 0.35f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profit_summary_kpi_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isHindi) "अनुमानित शुद्ध लाभ / Net Profit" else "Projected Net Profit",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = numberFormatter.format(result.netProfit),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = profitColor
                                )
                            }

                            // ROI Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isProfit) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.errorContainer)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = if (isProfit) Color(0xFF15803D) else MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${result.returnOnInvestmentPercent.toInt()}% ROI",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isProfit) Color(0xFF15803D) else MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // SUB METRICS ROW (Per acre profit, gross revenue, total cost)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "Profit / Acre", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = numberFormatter.format(result.netProfitPerAcre),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = profitColor
                                    )
                                }
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "Total Expense", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = numberFormatter.format(result.totalCost),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "Yield (Qtl)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "${result.expectedTotalYieldQuintals.toInt()} Q",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Break-even and Market Rate line
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Market Rate: ₹${result.sellingPricePerQuintal.toInt()}/Q",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Break-Even: ₹${result.breakEvenPricePerQuintal.toInt()}/Q",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // VOICE AUDIO READOUT
                        VoiceAdvisoryButton(
                            textToSpeak = speechText,
                            isSpeaking = uiState.isSpeaking,
                            onSpeak = onSpeak,
                            onStop = onStopSpeech,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // SHARE FINANCIAL PLAN
                        OutlinedButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "CropAI Farm Profit Plan: ${result.cropName}")
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        """
                                        🌾 CropAI Farm Profit & Economic Advisory
                                        ━━━━━━━━━━━━━━━━━━━━━━━
                                        Crop: ${result.cropName}
                                        Area: ${result.acreage} Acres
                                        
                                        💰 Total Cultivation Cost: ${numberFormatter.format(result.totalCost)}
                                        🌾 Expected Harvest Yield: ${result.expectedTotalYieldQuintals} Quintals
                                        💵 Estimated Gross Revenue: ${numberFormatter.format(result.grossRevenue)}
                                        
                                        ✅ Projected Net Profit: ${numberFormatter.format(result.netProfit)}
                                        📈 Net Profit / Acre: ${numberFormatter.format(result.netProfitPerAcre)}
                                        📊 Return on Investment (ROI): ${result.returnOnInvestmentPercent.toInt()}%
                                        🎯 Break-Even Price: ₹${result.breakEvenPricePerQuintal.toInt()}/Quintal
                                        
                                        Prepared via CropAI Agricultural Decision Support
                                        """.trimIndent()
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Budget Plan"))
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("share_profit_plan_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isHindi) "बजट रिपोर्ट शेयर करें / Share Plan" else "Share Economic Projection", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // STEP 4: EDITABLE COST BREAKDOWN DRAWER / CARD
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(TranslationKey.COST_BREAKDOWN),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        IconButton(onClick = { showCostEditor = !showCostEditor }) {
                            Icon(
                                imageVector = if (showCostEditor) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle Cost Details"
                            )
                        }
                    }

                    AnimatedVisibility(visible = showCostEditor) {
                        Column(
                            modifier = Modifier.padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.customCosts?.let { costs ->
                                CostItemRow(
                                    label = if (isHindi) "बीज की लागत (प्रति एकड़)" else "Seeds Cost (per Acre)",
                                    amount = costs.seedCostPerAcre
                                )
                                CostItemRow(
                                    label = if (isHindi) "उर्वरक एवं खाद (प्रति एकड़)" else "Fertilizer & Manure (per Acre)",
                                    amount = costs.fertilizerCostPerAcre
                                )
                                CostItemRow(
                                    label = if (isHindi) "सिंचाई व्यय (प्रति एकड़)" else "Irrigation & Pumping (per Acre)",
                                    amount = costs.irrigationCostPerAcre
                                )
                                CostItemRow(
                                    label = if (isHindi) "मजदूरी एवं जुताई (प्रति एकड़)" else "Labor & Tillage (per Acre)",
                                    amount = costs.laborAndTillageCostPerAcre
                                )
                                CostItemRow(
                                    label = if (isHindi) "कीटनाशक व फफूंदनाशक" else "Pesticides & Bio-control",
                                    amount = costs.pesticideCostPerAcre
                                )
                                CostItemRow(
                                    label = if (isHindi) "कटाई व थ्रेशिंग मशीनरी" else "Harvesting & Machinery",
                                    amount = costs.harvestingMachineryCostPerAcre
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CostItemRow(label: String, amount: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = "₹${amount.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
