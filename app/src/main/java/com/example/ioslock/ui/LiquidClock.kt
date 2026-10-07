package com.example.ioslock.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Date

// ============================================================
// HORLOGE LIQUID GLASS
// ============================================================

@Composable
fun LiquidClock(
    date: Date,
    fontKey: String,
    colorKey: String,
    scale: Float,
    is24h: Boolean,
    glassIntensity: Float = 0.6f,
    glassThickness: Float = 1.0f,
    hapticEnabled: Boolean = true,
    tinted: Boolean = false,
    modifier: Modifier = Modifier,
    showDate: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val color = clockColorFromKey(colorKey)
    val font = clockFontFromKey(fontKey)
    val weight = clockFontWeightFromKey(fontKey)

    val baseClockSize = 90.sp
    val baseDateSize = 18.sp
    val clockSize = (baseClockSize.value * scale).sp
    val dateSize = (baseDateSize.value * scale.coerceIn(0.85f, 1.3f)).sp

    val glassActive = glassIntensity > 0.01f
    val cornerRadius: Dp = (24.dp * scale.coerceIn(0.8f, 1.4f))

    // Haptique une seule fois à l'affichage si glass actif
    if (hapticEnabled && glassActive) {
        remember(glassActive) {
            triggerHapticMove(haptic)
            true
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .then(
                if (glassActive) {
                    Modifier
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.20f * glassIntensity),
                                    Color.White.copy(alpha = 0.06f * glassIntensity)
                                )
                            )
                        )
                        .then(
                            if (tinted) {
                                Modifier.background(Color(0xFF0A84FF).copy(alpha = 0.18f * glassIntensity))
                            } else Modifier
                        )
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.30f * glassIntensity),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.15f * glassIntensity)
                                )
                            )
                        )
                        .border(
                            width = (0.6.dp * glassThickness.coerceIn(0.5f, 2f)),
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.55f * glassIntensity),
                                    Color.White.copy(alpha = 0.10f),
                                    Color.Black.copy(alpha = 0.25f * glassIntensity)
                                )
                            ),
                            shape = RoundedCornerShape(cornerRadius)
                        )
                } else {
                    Modifier
                }
            )
            .padding(
                horizontal = (28.dp * scale.coerceIn(0.8f, 1.3f)),
                vertical = (10.dp * scale.coerceIn(0.8f, 1.3f))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (showDate) {
                Text(
                    text = clockFormatDate(date),
                    color = color.copy(alpha = 0.9f),
                    fontSize = dateSize,
                    fontFamily = font,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height((4 * scale).dp))
            }
            Text(
                text = clockFormatTime(date, is24h),
                color = color,
                fontSize = clockSize,
                fontFamily = font,
                fontWeight = weight,
                letterSpacing = (-2 * scale).sp
            )
        }
    }
}

// ============================================================
// HAPTIC HELPERS
// ============================================================

fun triggerHapticMove(haptic: HapticFeedback) {
    try {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    } catch (_: Exception) { }
}

fun triggerHapticLongPress(haptic: HapticFeedback) {
    try {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    } catch (_: Exception) { }
}

fun triggerHapticToggle(haptic: HapticFeedback, on: Boolean) {
    try {
        // ToggleOn/ToggleOff pas dispo sur cette version de Compose
        // On utilise LongPress comme approximation
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    } catch (_: Exception) { }
}
