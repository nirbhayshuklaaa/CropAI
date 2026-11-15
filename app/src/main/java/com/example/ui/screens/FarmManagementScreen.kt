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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.TranslationKey
import com.example.core.i18n.stringResource
import com.example.ui.UiState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmManagementScreen(
    uiState: UiState,
    onBack: () -> Unit,
    onNavigateHome: () -> Unit = {},
    onAddField: (String, Double, String) -> Unit = { _, _, _ -> },
    onDeleteField: (String) -> Unit = {}
) {
    val isHindi = uiState.currentLanguage == AppLanguage.HINDI
    val fieldList = uiState.farms.flatMap { it.fields }

    var showAddDialog by remember { mutableStateOf(false) }
    var newFieldName by remember { mutableStateOf("") }
    var newAcreage by remember { mutableStateOf("") }
    var newCrop by remember { mutableStateOf("") }

    val totalAreaFormatted = String.format(Locale.US, "%.1f", fieldList.sumOf { it.acreage })

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(TranslationKey.MY_FARM), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("farm_screen_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateHome, modifier = Modifier.testTag("farm_screen_home_button")) {
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
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Farm Summary Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Agriculture,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "${uiState.farmerProfile.name} जी का खेत" else "${uiState.farmerProfile.name}'s Farm",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${uiState.farmerProfile.village}, ${uiState.farmerProfile.district}, ${uiState.farmerProfile.state}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isHindi)
                            "कुल खेत: ${fieldList.size}  |  कुल रकबा: $totalAreaFormatted एकड़"
                        else
                            "Total Plots: ${fieldList.size}  |  Total Area: $totalAreaFormatted Acres",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isHindi) "पंजीकृत खेत और प्लाट" else "Registered Fields & Plots",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showAddDialog = !showAddDialog },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_field_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (showAddDialog) {
                            if (isHindi) "बंद करें" else "Close"
                        } else {
                            if (isHindi) "खेत जोड़ें" else "Add Field"
                        }
                    )
                }
            }

            if (showAddDialog) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = newFieldName,
                            onValueChange = { newFieldName = it },
                            label = { Text(if (isHindi) "खेत / प्लाट का नाम" else "Field Name (e.g. River Plot)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_field_name_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newAcreage,
                                onValueChange = { newAcreage = it },
                                label = { Text(if (isHindi) "रकबा (एकड़)" else "Acreage (Acres)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("new_field_acreage_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = newCrop,
                                onValueChange = { newCrop = it },
                                label = { Text(if (isHindi) "वर्तमान फसल" else "Current Crop") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("new_field_crop_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                        Button(
                            onClick = {
                                if (newFieldName.isNotBlank()) {
                                    val acre = newAcreage.toDoubleOrNull()?.coerceAtLeast(0.1) ?: 1.0
                                    val crop = newCrop.ifBlank { if (isHindi) "अनिर्धारित" else "Unassigned" }
                                    onAddField(newFieldName.trim(), acre, crop.trim())
                                    newFieldName = ""
                                    newAcreage = ""
                                    newCrop = ""
                                    showAddDialog = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("save_field_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isHindi) "खेत सेव करें" else "Save Field", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (fieldList.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("empty_fields_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = if (isHindi) "अभी कोई खेत या प्लाट दर्ज नहीं है" else "No fields or plots added yet",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isHindi) "नया प्लाट जोड़ने के लिए 'खेत जोड़ें' बटन दबाएं।" else "Tap 'Add Field' above to register your plot.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(fieldList, key = { it.id }) { field ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("field_card_${field.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = field.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = if (isHindi) "${field.acreage} एकड़" else "${field.acreage} Acres",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (isHindi)
                                            "वर्तमान फसल: ${field.currentCrop ?: "बोई नहीं गई"}"
                                        else
                                            "Current Crop: ${field.currentCrop ?: "Not planted"}",
                                        fontSize = 13.sp
                                    )
                                    if (field.previousCrops.isNotEmpty()) {
                                        Text(
                                            text = if (isHindi)
                                                "पिछली फसल: ${field.previousCrops.joinToString(", ")}"
                                            else
                                                "Previous: ${field.previousCrops.joinToString(", ")}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onDeleteField(field.id) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("delete_field_button_${field.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete ${field.name}",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
