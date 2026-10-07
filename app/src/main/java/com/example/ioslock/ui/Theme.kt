package com.example.ioslock.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================
// COULEURS
// ============================================================
object IOSColors {
    val Background = Color(0xFF0A0A0F)
    val Surface = Color(0xFF1C1C22)
    val Accent = Color(0xFF0A84FF)
    val AccentSecondary = Color(0xFF3A3A3C)
    val TextPrimary = Color.White
    val TextSecondary = Color.White.copy(alpha = 0.7f)
    val TextTertiary = Color.White.copy(alpha = 0.5f)
    val Error = Color(0xFFFF453A)

    val GlassLight = Color.White.copy(alpha = 0.15f)
    val GlassBorder = Color.White.copy(alpha = 0.25f)
    val GlassDark = Color(0x99000000)
    val Overlay = Color(0x66000000)

    // Liquid Glass (iOS 26)
    val HighlightTop = Color.White.copy(alpha = 0.35f)
    val HighlightMid = Color.White.copy(alpha = 0.10f)
    val ShadowBottom = Color.Black.copy(alpha = 0.25f)
    val EdgeLight = Color.White.copy(alpha = 0.45f)
    val EdgeShadow = Color.Black.copy(alpha = 0.30f)
}

// ============================================================
// TYPOGRAPHIE
// ============================================================
object IOSTypography {
    val ClockSize = 90.sp
    val ClockLetterSpacing = (-2).sp
    val LargeTitleSize = 34.sp
    val DateSize = 18.sp
    val TitleSize = 28.sp
    val BodySize = 15.sp
    val CaptionSize = 12.sp
}

// ============================================================
// DIMENSIONS
// ============================================================
object IOSDimensions {
    val CornerRadius = 16.dp
    val CornerRadiusLarge = 24.dp
    val CornerRadiusXL = 32.dp
    val Padding = 16.dp
    val ButtonHeight = 50.dp
}

// ============================================================
// MODIFIER HELPERS
// ============================================================
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = this.then(
    Modifier.clickable(
        interactionSource = MutableInteractionSource(),
        indication = null,
        onClick = onClick
    )
)

// ============================================================
// LIQUID GLASS — le vrai effet iOS 26
// ============================================================

/**
 * Un container Liquid Glass : fond translucide + dégradé diagonal
 * qui simule un reflet, + bordure lumineuse en haut, + ombre en bas.
 *
 * Sur Android 12+, on peut ajouter un vrai flou de ce qui est derrière
 * via `Modifier.blur()`, mais ce n'est pas possible sans capturer
 * le fond en temps réel — on simule donc un reflet pour donner
 * l'illusion de "verre épais".
 */
@Composable
fun LiquidGlass(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = IOSDimensions.CornerRadiusLarge,
    tint: Color = Color.Transparent,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            // 1. Couche de base : dégradé translucide
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.08f)
                    )
                )
            )
            // 2. Teinte optionnelle (bleu iOS par exemple)
            .then(
                if (tint != Color.Transparent)
                    Modifier.background(tint.copy(alpha = 0.55f))
                else Modifier
            )
            // 3. Reflet diagonal (highlight)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        IOSColors.HighlightTop,
                        IOSColors.HighlightMid,
                        Color.Transparent,
                        IOSColors.ShadowBottom
                    )
                )
            )
            // 4. Bordure lumineuse (effet verre)
            .border(
                width = 0.7.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        IOSColors.EdgeLight,
                        Color.White.copy(alpha = 0.10f),
                        IOSColors.EdgeShadow
                    )
                ),
                shape = shape
            ),
        content = content
    )
}

/**
 * Bouton Liquid Glass : capsule arrondie, reflet, bordure.
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
    enabled: Boolean = true
) {
    val tint = if (accent) IOSColors.Accent else Color.Transparent
    val textColor = if (enabled) Color.White else Color.White.copy(alpha = 0.4f)

    LiquidGlass(
        modifier = modifier
            .then(if (enabled) Modifier.clickableNoRipple(onClick) else Modifier),
        cornerRadius = 50.dp,
        tint = tint
    ) {
        Box(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text,
                color = textColor,
                fontSize = IOSTypography.BodySize,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Ancienne API conservée pour compatibilité avec le code existant.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = IOSDimensions.CornerRadiusLarge,
    content: @Composable BoxScope.() -> Unit
) {
    LiquidGlass(
        modifier = modifier,
        cornerRadius = cornerRadius,
        content = content
    )
}
