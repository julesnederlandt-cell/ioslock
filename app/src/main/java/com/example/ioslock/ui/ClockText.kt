package com.example.ioslock.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Composant d'horloge réutilisable.
 * Applique les styles choisis par l'utilisateur.
 */

// ============================================================
// MAPPINGS UTILITAIRES
// ============================================================

fun clockColorFromKey(key: String): Color = when (key) {
    "white" -> Color.White
    "black" -> Color.Black
    "blue" -> Color(0xFF0A84FF)
    "yellow" -> Color(0xFFFFD60A)
    "pink" -> Color(0xFFFF2D55)
    "green" -> Color(0xFF30D158)
    "orange" -> Color(0xFFFF9F0A)
    else -> Color.White
}

fun clockFontFromKey(key: String): FontFamily = when (key) {
    "serif" -> FontFamily.Serif
    "mono" -> FontFamily.Monospace
    "cursive" -> FontFamily.Cursive
    "light" -> FontFamily.SansSerif
    else -> FontFamily.Default
}

fun clockFontWeightFromKey(key: String): FontWeight = when (key) {
    "light" -> FontWeight.ExtraLight
    "serif" -> FontWeight.Normal
    else -> FontWeight.Light
}

fun clockFormatTime(date: Date, is24h: Boolean): String {
    val pattern = if (is24h) "HH:mm" else "h:mm"
    return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
}

fun clockFormatDate(date: Date): String {
    return SimpleDateFormat("EEEE d MMMM", Locale.FRENCH).format(date)
}

// ============================================================
// COMPOSANT
// ============================================================

@Composable
fun ClockText(
    date: Date,
    fontKey: String,
    colorKey: String,
    scale: Float,
    is24h: Boolean,
    modifier: Modifier = Modifier,
    showDate: Boolean = true
) {
    val color = clockColorFromKey(colorKey)
    val font = clockFontFromKey(fontKey)
    val weight = clockFontWeightFromKey(fontKey)

    val baseClockSize = 90.sp
    val baseDateSize = 18.sp

    val clockSize: TextUnit = (baseClockSize.value * scale).sp
    val dateSize: TextUnit = (baseDateSize.value * scale.coerceIn(0.85f, 1.3f)).sp

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showDate) {
            Text(
                text = clockFormatDate(date),
                color = color.copy(alpha = 0.95f),
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
