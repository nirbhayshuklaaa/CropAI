package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.TranslationKey
import com.example.core.i18n.stringResource

@Composable
fun VoiceAdvisoryButton(
    textToSpeak: String,
    isSpeaking: Boolean,
    onSpeak: (String) -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    if (isSpeaking) {
        Button(
            onClick = onStop,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = modifier
                .scale(pulseScale)
                .testTag("stop_voice_advisory_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop Voice Audio",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(TranslationKey.VOICE_READOUT_STOP),
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 12.sp else 13.sp
                )
            }
        }
    } else {
        OutlinedButton(
            onClick = { onSpeak(textToSpeak) },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            ),
            modifier = modifier.testTag("listen_voice_advisory_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Listen Advisory",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(TranslationKey.VOICE_READOUT_LISTEN),
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 12.sp else 13.sp
                )
            }
        }
    }
}
