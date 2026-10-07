package com.example.ioslock.ui

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Date

// ============================================================
// FORMES DISPONIBLES
// ============================================================
object ClockShapes {
    const val SQUARE = "square"        // 8dp
    const val ROUNDED = "rounded"      // 18dp
    const val VERY_ROUNDED = "very"    // 28dp
    const val CAPSULE = "capsule"      // 50dp (pill)
    const val SQUIRCLE = "squircle"    // 36dp (style iOS)
}

fun shapeToRadius(shape: String, baseScale: Float): Dp {
    val s = baseScale.coerceIn(0.8f, 1.4f)
    return when (shape) {
        ClockShapes.SQUARE -> 8.dp * s
        ClockShapes.ROUNDED -> 18.dp * s
        ClockShapes.VERY_ROUNDED -> 28.dp * s
        ClockShapes.CAPSULE -> 50.dp * s
        ClockShapes.SQUIRCLE -> 36.dp * s
        else -> 24.dp * s
    }
}

// ============================================================
// HORLOGE AVEC EFFET VERRE
// ============================================================
@Composable
fun LiquidClock(
    date: Date,
    fontKey: String,
    colorKey: String,
    scale: Float,
    is24h: Boolean,
    style: String = "capsule",
    shape: String = ClockShapes.ROUNDED,
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
    val cornerRadius = shapeToRadius(shape, scale)

    if (hapticEnabled && style != "classic") {
        remember(style) {
            triggerHapticMove(haptic)
            true
        }
    }

    when (style) {
        "classic" -> ClockTextContent(
            date = date, color = color, font = font, weight = weight,
            clockSize = clockSize, dateSize = dateSize, scale = scale,
            is24h = is24h, showDate = showDate,
            modifier = modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )

        "glass" -> Box(
            modifier = modifier
                .clip(RoundedCornerShape(cornerRadius))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.22f * glassIntensity),
                            Color.White.copy(alpha = 0.08f * glassIntensity)
                        )
                    )
                )
                .then(
                    if (tinted) Modifier.background(Color(0xFF0A84FF).copy(alpha = 0.15f * glassIntensity))
                    else Modifier
                )
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.35f * glassIntensity),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.18f * glassIntensity)
                        )
                    )
                )
                .border(
                    width = 0.8.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.7f),
                            Color.White.copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.25f)
                        )
                    ),
                    shape = RoundedCornerShape(cornerRadius)
                )
                .padding(
                    horizontal = (28.dp * scale.coerceIn(0.8f, 1.3f)),
                    vertical = (10.dp * scale.coerceIn(0.8f, 1.3f))
                ),
            contentAlignment = Alignment.Center
        ) {
            GlassTextStack(
                date = date, color = color, font = font, weight = weight,
                clockSize = clockSize, dateSize = dateSize, scale = scale,
                is24h = is24h, showDate = showDate,
                glassIntensity = glassIntensity
            )
        }

        else -> { // "capsule"
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.20f * glassIntensity),
                                Color.White.copy(alpha = 0.06f * glassIntensity)
                            )
                        )
                    )
                    .then(
                        if (tinted) Modifier.background(Color(0xFF0A84FF).copy(alpha = 0.18f * glassIntensity))
                        else Modifier
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
                        width = 0.6.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.55f * glassIntensity),
                                Color.White.copy(alpha = 0.10f),
                                Color.Black.copy(alpha = 0.25f * glassIntensity)
                            )
                        ),
                        shape = RoundedCornerShape(cornerRadius)
                    )
                    .padding(
                        horizontal = (28.dp * scale.coerceIn(0.8f, 1.3f)),
                        vertical = (10.dp * scale.coerceIn(0.8f, 1.3f))
                    ),
                contentAlignment = Alignment.Center
            ) {
                ClockTextContent(
                    date = date, color = color, font = font, weight = weight,
                    clockSize = clockSize, dateSize = dateSize, scale = scale,
                    is24h = is24h, showDate = showDate,
                    modifier = Modifier
                )
            }
        }
    }
}

// ============================================================
// TEXTE NORMAL (styles classic + capsule)
// ============================================================
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
// TEXTE "VERRE" : 3 couches avec aura floutée + reflet
// ============================================================
@Composable
private fun GlassTextStack(
    date: Date,
    color: Color,
    font: FontFamily,
    weight: FontWeight,
    clockSize: androidx.compose.ui.unit.TextUnit,
    dateSize: androidx.compose.ui.unit.TextUnit,
    scale: Float,
    is24h: Boolean,
    showDate: Boolean,
    glassIntensity: Float
) {
    val timeText = clockFormatTime(date, is24h)
    val dateText = clockFormatDate(date)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (showDate) {
            Text(
                text = dateText,
                color = Color.White.copy(alpha = 0.65f),
                fontSize = dateSize,
                fontFamily = font,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height((4 * scale).dp))
        }

        Box(contentAlignment = Alignment.Center) {
            // Couche 1 : AURA FLOUTÉE (donne l'épaisseur du verre)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Text(
                    text = timeText,
                    color = Color.White.copy(alpha = 0.5f * glassIntensity),
                    fontSize = clockSize,
                    fontFamily = font,
                    fontWeight = weight,
                    letterSpacing = (-2 * scale).sp,
                    modifier = Modifier
                        .graphicsLayer {
                            renderEffect = RenderEffect
                                .createBlurEffect(8f, 8f, Shader.TileMode.DECAL)
                                .asComposeRenderEffect()
                        }
                )
            } else {
                Text(
                    text = timeText,
                    color = Color.White.copy(alpha = 0.5f * glassIntensity),
                    fontSize = clockSize,
                    fontFamily = font,
                    fontWeight = weight,
                    letterSpacing = (-2 * scale).sp,
                    modifier = Modifier.blur(6.dp, BlurredEdgeTreatment.Unbounded)
                )
            }

            // Couche 2 : REMPLISSAGE TRANSLUCIDE
            Text(
                text = timeText,
                color = Color.White.copy(alpha = 0.45f),
                fontSize = clockSize,
                fontFamily = font,
                fontWeight = weight,
                letterSpacing = (-2 * scale).sp
            )

            // Couche 3 : REFLET DIAGONAL (brillance qui traverse les chiffres)
            Text(
                text = timeText,
                color = Color.Transparent,
                fontSize = clockSize,
                fontFamily = font,
                fontWeight = weight,
                letterSpacing = (-2 * scale).sp,
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.95f),
                            Color.White.copy(alpha = 0.2f),
                            Color.Transparent,
                            Color.White.copy(alpha = 0.4f)
                        )
                    )
                )
            )
        }
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
