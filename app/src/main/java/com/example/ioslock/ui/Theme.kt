package com.example.ioslock.ui

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
// COMPOSANTS GLASS
// ============================================================
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = IOSDimensions.CornerRadiusLarge,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.18f),
                        Color.White.copy(alpha = 0.08f)
                    )
                )
            )
            .border(
                width = 0.5.dp,
                color = IOSColors.GlassBorder,
                shape = RoundedCornerShape(cornerRadius)
            ),
        content = content
    )
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
    enabled: Boolean = true
) {
    val bg = when {
        !enabled -> Color.White.copy(alpha = 0.05f)
        accent -> IOSColors.Accent
        else -> Color.White.copy(alpha = 0.15f)
    }
    val borderColor = when {
        !enabled -> Color.White.copy(alpha = 0.1f)
        accent -> Color.White.copy(alpha = 0.3f)
        else -> IOSColors.GlassBorder
    }
    val textColor = if (enabled) Color.White else Color.White.copy(alpha = 0.4f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(0.5.dp, borderColor, RoundedCornerShape(50))
            .then(if (enabled) Modifier.clickableNoRipple(onClick) else Modifier)
            .padding(vertical = 14.dp, horizontal = 24.dp),
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
