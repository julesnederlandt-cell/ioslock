package com.example.ioslock.ui

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
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
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Date

// ============================================================
// SHADER AGSL pour l'effet "verre liquide" iOS 26
// Simule une lentille de verre : réfraction + léger flou + reflet
// ============================================================
private const val LIQUID_GLASS_SHADER = """
uniform shader uContent;
uniform float2 uSize;
uniform float uIntensity;
uniform float uThickness;
uniform float uTime;

half4 main(float2 coord) {
    // Coordonnées normalisées (0..1)
    float2 uv = coord / uSize;

    // Distance au centre (pour la courbure de la lentille)
    float2 centered = uv - 0.5;
    float dist = length(centered);

    // Intensité de la courbure : plus on est près du bord, plus ça réfracte
    float edgeFactor = pow(dist * 2.0, 2.0) * uThickness * 0.3;

    // Décalage de réfraction vers l'extérieur
    float2 refractOffset = centered * edgeFactor;

    // Coordonnée décalée
    float2 refractCoord = coord - refractOffset * uSize;

    // Échantillonne le contenu décalé
    half4 baseColor = uContent.eval(refractCoord);

    // Reflet lumineux en haut-gauche (specular highlight)
    float2 lightDir = normalize(float2(-0.5, -0.7));
    float highlight = max(0.0, dot(normalize(centered + 0.001), lightDir));
    highlight = pow(highlight, 3.0) * uIntensity;

    // Ombre en bas-droite
    float2 shadowDir = normalize(float2(0.5, 0.7));
    float shadow = max(0.0, dot(normalize(centered + 0.001), shadowDir));
    shadow = pow(shadow, 2.0) * uIntensity * 0.4;

    // Bordure lumineuse
    float edge = smoothstep(0.42, 0.5, dist);
    float border = (1.0 - edge) * uIntensity;

    // Composition finale
    half4 result = baseColor;
    result.rgb += highlight * 0.6;
    result.rgb -= shadow * 0.3;
    result.rgb += border * 0.2;
    result.a = baseColor.a;

    return result;
}
"""

// ============================================================
// HORLOGE LIQUID GLASS
// ============================================================

/**
 * Horloge avec effet Liquid Glass iOS 26.
 *
 * @param glassIntensity Force de l'effet (0 = texte plat, 1 = verre intense)
 * @param glassThickness Épaisseur de la lentille (0 = plat, 2 = lentille épaisse)
 * @param hapticEnabled Active les vibrations subtiles
 * @param tinted Si true, teinte légère bleutée (comme iOS en mode teinté)
 */
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

    // Effet glass seulement si intensité > 0
    val glassActive = glassIntensity > 0.01f

    // Forme capsule
    val cornerRadius: Dp = (24.dp * scale.coerceIn(0.8f, 1.4f))

    // Déclenche une vibration quand on entre en mode glass (au premier affichage)
    if (hapticEnabled && glassActive) {
        remember(glassActive) {
            try {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Exception) { }
            true
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .then(
                if (glassActive) {
                    Modifier
                        // Couche de base : dégradé translucide
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.20f * glassIntensity),
                                    Color.White.copy(alpha = 0.06f * glassIntensity)
                                )
                            )
                        )
                        // Teinte bleutée optionnelle (iOS mode teinté)
                        .then(
                            if (tinted) {
                                Modifier.background(Color(0xFF0A84FF).copy(alpha = 0.18f * glassIntensity))
                            } else Modifier
                        )
                        // Reflet diagonal (highlight principal)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.30f * glassIntensity),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.15f * glassIntensity)
                                )
                            )
                        )
                        // Bordure lumineuse
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
// HAPTIC HELPERS (utilisables depuis EditScreen)
// ============================================================

fun triggerHapticMove(haptic: androidx.compose.ui.hapticfeedback.HapticFeedback) {
    try {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    } catch (_: Exception) { }
}

fun triggerHapticLongPress(haptic: androidx.compose.ui.hapticfeedback.HapticFeedback) {
    try {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    } catch (_: Exception) { }
}

fun triggerHapticToggle(haptic: androidx.compose.ui.hapticfeedback.HapticFeedback, on: Boolean) {
    try {
        if (on) haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
        else haptic.performHapticFeedback(HapticFeedbackType.ToggleOff)
    } catch (_: Exception) { }
}
