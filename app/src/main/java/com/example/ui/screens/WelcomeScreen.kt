package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.components.LanguagePickerDialog

@Composable
fun WelcomeScreen(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onStartOnboarding: () -> Unit = {},
    onCompleteOnboarding: (name: String, state: String, district: String, village: String) -> Unit
) {
    var showLanguagePicker by remember { mutableStateOf(false) }
    var farmerName by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("Uttar Pradesh") }
    var district by remember { mutableStateOf("Varanasi") }
    var village by remember { mutableStateOf("") }

    if (showLanguagePicker) {
        LanguagePickerDialog(
            currentLanguage = currentLanguage,
            onLanguageSelected = onLanguageSelected,
            onDismissRequest = { showLanguagePicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Breathtaking Agricultural Banner for Instant Connection
        Card(
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_farmer_hero),
                    contentDescription = "Vibrant Agricultural Field",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x22000000),
                                    Color(0x7700280B),
                                    Color(0xEE052E16)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "CropAI • किसान का साथी",
                        color = Color(0xFFA7F3D0),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "स्वागत है किसान भाई!",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "उन्नत तकनीक, सही सलाह, अधिक पैदावार",
                        color = Color(0xFFD1FAE5),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Language Switcher Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(TranslationKey.SELECT_LANGUAGE),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${currentLanguage.nativeName} (${currentLanguage.displayName})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                OutlinedButton(
                    onClick = { showLanguagePicker = true },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("change_language_button")
                ) {
                    Text(text = "Change / बदलें", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Onboarding Form Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(TranslationKey.WELCOME_FARMER),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = farmerName,
                    onValueChange = { farmerName = it },
                    label = { Text(stringResource(TranslationKey.NAME_LABEL)) },
                    placeholder = { Text("e.g. Ramesh Singh / रमेश सिंह") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("farmer_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text(stringResource(TranslationKey.STATE_LABEL)) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("state_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text(stringResource(TranslationKey.DISTRICT_LABEL)) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("district_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = village,
                    onValueChange = { village = it },
                    label = { Text(stringResource(TranslationKey.VILLAGE_LABEL)) },
                    placeholder = { Text("e.g. Anandpur") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("village_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        onCompleteOnboarding(farmerName, state, district, village)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("get_started_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(TranslationKey.GET_STARTED),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(TranslationKey.DECISION_SUPPORT_DISCLAIMER),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
