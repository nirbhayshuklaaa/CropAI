package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Science
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.TranslationKey
import com.example.core.i18n.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeCenterScreen(
    onBack: () -> Unit,
    onNavigateHome: () -> Unit = {}
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(TranslationKey.FARMING_GUIDE), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateHome, modifier = Modifier.testTag("guide_screen_home_button")) {
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            KnowledgeCard(
                icon = Icons.Default.CameraAlt,
                title = "How to Take Soil Photos Correctly",
                content = "1. Dig 15 cm deep into topsoil at 4-5 field spots.\n2. Mix samples thoroughly in a clean bucket.\n3. Take a representative handful and spread on clean soil surface or plain sheet.\n4. Ensure natural overhead daylight without sharp tree or hand shadows.\n5. Keep camera lens directly facing the soil."
            )

            KnowledgeCard(
                icon = Icons.Default.Science,
                title = "Understanding N-P-K & Soil pH",
                content = "• Nitrogen (N): Drives leaf chlorophyll and early vegetative vigor.\n• Phosphorus (P): Promotes deep root establishment and early flowering.\n• Potassium (K): Strengthens cell walls, protects against drought, pests and lodging.\n• pH Level: 6.0 to 7.5 is ideal. Strongly acidic soils (<5.5) lock phosphorus; alkaline soils (>8.0) lock micronutrients."
            )

            KnowledgeCard(
                icon = Icons.Default.Loop,
                title = "Scientific Crop Rotation Rules",
                content = "• Never plant heavy nutrient feeders (e.g., paddy, sugarcane) back-to-back.\n• Follow cereal crops with leguminous pulses (chickpea, arhar, moong) to fix biological nitrogen.\n• Rotate shallow-rooted crops with deep taproot crops to utilize soil nutrients at multiple depths."
            )

            KnowledgeCard(
                icon = Icons.Default.AccountBalance,
                title = "Key Government Agricultural Schemes",
                content = "• Soil Health Card Scheme: Provides free laboratory soil testing and nutrient recommendations every 2 years via your local block agriculture office.\n• PM-KISAN (Pradhan Mantri Kisan Samman Nidhi): Provides income support of ₹6,000/year to eligible landholder farmer families.\n• PM Krishi Sinchayee Yojana (PMKSY): Subsidies up to 55% for drip and sprinkler micro-irrigation systems."
            )

            KnowledgeCard(
                icon = Icons.Default.Info,
                title = "Decision Support Disclaimer",
                content = "CropAI is an advisory decision-support companion designed for Indian farmers. While AI models provide helpful guidance, always verify with your local Krishi Vigyan Kendra (KVK) or soil testing laboratory before major chemical interventions."
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun KnowledgeCard(icon: ImageVector, title: String, content: String) {
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
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
