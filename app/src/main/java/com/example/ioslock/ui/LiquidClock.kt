package com.example.ioslock.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.nadeemiqbal.liquidglass.GlassCard
import io.github.nadeemiqbal.liquidglass.LiquidGlassState
import java.util.Date

/**
 * Horloge utilisant la lib liquid-glass.
 *
 * @param state Le LiquidGlassState partagé (créé dans l'écran parent via rememberLiquidGlassState)
 */
@Composable
fun LiquidClock(
    date: Date,
    fontKey: String,
    colorKey: String,
    scale: Float,
    is24h: Boolean,
    glassState: LiquidGlassState,
    style: String = "capsule",
    glassIntensity: Float = 0.6f,
    hapticEnabled: Boolean = true,
    tinted: Boolean = false,
    modifier: Modifier = Modifier,
    showDate: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val color = clockColorFromKey(colorKey)
    val font = clockFontFromKey(fontKey)
    val weight = clockFontWeightFromKey(fontKey)

    val clockSize = (90f * scale).sp
    val dateSize = (18f * scale.coerceIn(0.85f, 1.3f)).sp
    val cornerRadius: Dp = (24.dp * scale.coerceIn(0.8f, 1.4f))

    if (hapticEnabled && style != "classic") {
        remember(style) {
            triggerHapticMove(haptic)
            true
        }
    }

    when (style) {
        "classic" -> {
            ClockTextContent(
                date = date,
                color = color,
                font = font,
                weight = weight,
                clockSize = clockSize,
                dateSize = dateSize,
                scale = scale,
                is24h = is24h,
                showDate = showDate,
                modifier = modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            )
        }
        "capsule", "glass" -> {
            // Utilise GlassCard de la lib liquid-glass
            // glassIntensity contrôle le tint alpha (0.0 → 1.0)
            val tintColor = if (tinted) {
                Color(0xFF0A84FF).copy(alpha = 0.18f * glassIntensity)
            } else {
                Color.White.copy(alpha = 0.12f * glassIntensity)
            }

            GlassCard(
                state = glassState,
                modifier = modifier,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius),
                tint = tintColor
            ) {
                Box(
                    modifier = Modifier.padding(
                        horizontal = (28.dp * scale.coerceIn(0.8f, 1.3f)),
                        vertical = (10.dp * scale.coerceIn(0.8f, 1.3f))
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    ClockTextContent(
                        date = date,
                        color = color,
                        font = font,
                        weight = weight,
                        clockSize = clockSize,
                        dateSize = dateSize,
                        scale = scale,
                        is24h = is24h,
                        showDate = showDate,
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

@Composable
private fun ClockTextContent(
    date: Date,
    color: Color,
    font: FontFamily,
    weight: FontWeight,
    clockSize: androidx.compose.ui.unit.TextUnit,
    dateSize: androidx.compose.ui.unit.TextUnit,
    scale: Float,
    is24h: Boolean,
    showDate: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

// ============================================================
// HAPTIC HELPERS
// ============================================================
fun triggerHapticMove(haptic: HapticFeedback) {
    try { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) } catch (_: Exception) {}
}

fun triggerHapticLongPress(haptic: HapticFeedback) {
    try { haptic.performHapticFeedback(HapticFeedbackType.LongPress) } catch (_: Exception) {}
}

fun triggerHapticToggle(haptic: HapticFeedback, on: Boolean) {
    try { haptic.performHapticFeedback(HapticFeedbackType.LongPress) } catch (_: Exception) {}
}
