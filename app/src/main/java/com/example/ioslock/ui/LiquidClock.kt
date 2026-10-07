package com.example.ioslock.ui

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Date

// ============================================================
// SHADER AGSL : EFFET VERRE LIQUIDE (SDF + Bevel Lighting)
// Simule une lentille de verre avec réfraction et éclat
// ============================================================
private const val GLASS_SHADER = """
uniform shader uContent;
uniform float2 uSize;
uniform float uIntensity;
uniform float uThickness;

float sdRoundedBox(float2 p, float2 b, float r) {
    float2 q = abs(p) - b + r;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

half4 main(float2 coord) {
    float2 uv = coord / uSize;
    float2 centered = uv - 0.5;

    // SDF pour la forme globale (capsule)
    float2 b = float2(0.5, 0.5) - 0.35;
    float d = sdRoundedBox(centered, b, 0.35);

    // Normale à partir du gradient du SDF
    float2 grad = float2(dFdx(d), dFdy(d));
    float2 normal = normalize(grad + float2(0.0001, 0.0001));

    // Lumière venant du haut-gauche
    float2 lightDir = normalize(float2(-0.5, -0.7));
    float diffuse = max(0.0, dot(normal, lightDir));
    float specular = pow(diffuse, 3.0);

    // Réfraction : on décale légèrement l'échantillonnage du contenu derrière
    float edgeFactor = smoothstep(0.0, 0.06, abs(d));
    float2 refractOffset = normal * edgeFactor * 0.025 * uIntensity;
    half4 baseColor = uContent.eval(coord + refractOffset * uSize);

    // Bord lumineux
    float border = 1.0 - smoothstep(0.0, 0.015, abs(d));
    float3 borderGlow = float3(1.0) * border * uIntensity * 0.4;

    // Composition finale
    half4 result = baseColor;
    result.rgb += specular * 0.35 * uIntensity;
    result.rgb += borderGlow;
    result.a = baseColor.a;

    return result;
}
"""

// ============================================================
// HORLOGE — 3 STYLES AU CHOIX
// ============================================================

/**
 * Horloge avec 3 styles :
 * - "classic" : texte uni, pas de capsule
 * - "capsule" : capsule translucide + chiffres normaux
 * - "glass"   : chiffres translucides avec effet verre (shader AGSL)
 */
@Composable
fun LiquidClock(
    date: Date,
    fontKey: String,
    colorKey: String,
    scale: Float,
    is24h: Boolean,
    style: String = "capsule",
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

    val clockSize = (90f * scale).sp
    val dateSize = (18f * scale.coerceIn(0.85f, 1.3f)).sp
    val cornerRadius: Dp = (24.dp * scale.coerceIn(0.8f, 1.4f))

    // Haptique à l'affichage si verre actif
    if (hapticEnabled && style != "classic") {
        remember(style) {
            triggerHapticMove(haptic)
            true
        }
    }

    when (style) {
        "classic" -> {
            ClockContent(
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
        "capsule" -> {
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(cornerRadius))
                    .then(
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
                    )
                    .padding(
                        horizontal = (28.dp * scale.coerceIn(0.8f, 1.3f)),
                        vertical = (10.dp * scale.coerceIn(0.8f, 1.3f))
                    ),
                contentAlignment = Alignment.Center
            ) {
                ClockContent(
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
        "glass" -> {
            // Style "verre" : chiffres translucides avec shader AGSL
            // Le shader applique une réfraction + reflet sur la zone de l'horloge
            var shaderReady by remember { mutableStateOf(false) }

            Box(
                modifier = modifier
                    .graphicsLayer {
                        // Applique le shader AGSL sur Android 13+
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            try {
                                val shader = RuntimeShader(GLASS_SHADER)
                                shader.setFloatUniform("uSize", size.width, size.height)
                                shader.setFloatUniform("uIntensity", glassIntensity)
                                shader.setFloatUniform("uThickness", glassThickness)
                                renderEffect = RenderEffect
                                    .createRuntimeShaderEffect(shader, "uContent")
                                    .asComposeRenderEffect()
                                shaderReady = true
                            } catch (e: Exception) {
                                // Fallback silencieux si le shader échoue
                                shaderReady = false
                            }
                        }
                    }
                    .padding(
                        horizontal = (28.dp * scale.coerceIn(0.8f, 1.3f)),
                        vertical = (10.dp * scale.coerceIn(0.8f, 1.3f))
                    ),
                contentAlignment = Alignment.Center
            ) {
                GlassClockContent(
                    date = date,
                    color = color,
                    font = font,
                    weight = weight,
                    clockSize = clockSize,
                    dateSize = dateSize,
                    scale = scale,
                    is24h = is24h,
                    showDate = showDate,
                    glassIntensity = glassIntensity,
                    tinted = tinted
                )
            }
        }
    }
}

// ============================================================
// CONTENU DE L'HORLOGE — version texte normal (styles classic + capsule)
// ============================================================
@Composable
private fun ClockContent(
    date: Date,
    color: Color,
    font: androidx.compose.ui.text.font.FontFamily,
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
// CONTENU DE L'HORLOGE — version "verre" (chiffres translucides)
// ============================================================
@Composable
private fun GlassClockContent(
    date: Date,
    color: Color,
    font: androidx.compose.ui.text.font.FontFamily,
    weight: FontWeight,
    clockSize: androidx.compose.ui.unit.TextUnit,
    dateSize: androidx.compose.ui.unit.TextUnit,
    scale: Float,
    is24h: Boolean,
    showDate: Boolean,
    glassIntensity: Float,
    tinted: Boolean
) {
    // Teinte de base du texte "verre" : blanc translucide
    val glassTextColor = if (tinted) {
        Color(0xFFBFE1FF).copy(alpha = 0.55f)
    } else {
        Color.White.copy(alpha = 0.55f)
    }

    // Bordure lumineuse pour simuler l'épaisseur
    val glassBorderColor = Color.White.copy(alpha = 0.85f)

    // Reflet diagonal qui traverse les chiffres
    val glassHighlight = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.9f),
            Color.White.copy(alpha = 0.2f),
            Color.Transparent,
            Color.White.copy(alpha = 0.3f)
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showDate) {
            Text(
                text = clockFormatDate(date),
                color = glassTextColor,
                fontSize = dateSize,
                fontFamily = font,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height((4 * scale).dp))
        }

        Box(contentAlignment = Alignment.Center) {
            // Couche 1 : contour épais (donne l'épaisseur du verre)
            Text(
                text = clockFormatTime(date, is24h),
                color = glassBorderColor.copy(alpha = 0.5f * glassIntensity),
                fontSize = clockSize,
                fontFamily = font,
                fontWeight = weight,
                letterSpacing = (-2 * scale).sp,
                style = TextStyle(
                    drawStyle = Stroke(width = 4f * scale)
                )
            )

            // Couche 2 : remplissage translucide
            Text(
                text = clockFormatTime(date, is24h),
                color = glassTextColor,
                fontSize = clockSize,
                fontFamily = font,
                fontWeight = weight,
                letterSpacing = (-2 * scale).sp
            )

            // Couche 3 : reflet diagonal (via alpha sur un texte blanc)
            Text(
                text = clockFormatTime(date, is24h),
                color = Color.Transparent,
                fontSize = clockSize,
                fontFamily = font,
                fontWeight = weight,
                letterSpacing = (-2 * scale).sp,
                style = TextStyle(
                    brush = glassHighlight,
                    textAlign = TextAlign.Center
                )
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
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    } catch (_: Exception) { }
}
