package com.example.ioslock.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    val GlassDark = Color(0x99000000)
}

object IOSTypography {
    val ClockSize = 90.sp
    val ClockLetterSpacing = (-2).sp
    val DateSize = 18.sp
    val TitleSize = 28.sp
    val BodySize = 15.sp
    val CaptionSize = 12.sp
}

object IOSDimensions {
    val CornerRadius = 16.dp
    val CornerRadiusLarge = 24.dp
    val Padding = 16.dp
    val ButtonHeight = 50.dp
}
