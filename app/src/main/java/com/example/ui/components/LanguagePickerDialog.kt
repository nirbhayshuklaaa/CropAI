package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.TranslationKey
import com.example.core.i18n.stringResource

@Composable
fun LanguagePickerDialog(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(TranslationKey.SELECT_LANGUAGE),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppLanguage.entries.forEach { lang ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLanguageSelected(lang)
                                onDismissRequest()
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentLanguage == lang)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = lang.nativeName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentLanguage == lang)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = lang.displayName,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                            RadioButton(
                                selected = (currentLanguage == lang),
                                onClick = {
                                    onLanguageSelected(lang)
                                    onDismissRequest()
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(TranslationKey.BACK))
            }
        }
    )
}
