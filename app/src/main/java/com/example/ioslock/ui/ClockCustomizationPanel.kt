package com.example.ioslock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ClockCustomizationPanel(
    fontKey: String,
    colorKey: String,
    scale: Float,
    is24h: Boolean,
    glassIntensity: Float,
    glassTinted: Boolean,
    hapticEnabled: Boolean,
    clockStyle: String,
    clockShape: String,
    onFontChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onScaleChange: (Float) -> Unit,
    on24hChange: (Boolean) -> Unit,
    onGlassIntensityChange: (Float) -> Unit,
    onGlassTintedChange: (Boolean) -> Unit,
    onHapticEnabledChange: (Boolean) -> Unit,
    onClockStyleChange: (String) -> Unit,
    onClockShapeChange: (String) -> Unit,
    onClose: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color(0xEE1C1C22))
            .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Box(
            Modifier.align(Alignment.CenterHorizontally).width(40.dp).height(4.dp)
                .clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.3f))
        )
        Spacer(Modifier.height(16.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Personnaliser l'horloge", color = IOSColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .clickableNoRipple {
                        if (hapticEnabled) triggerHapticToggle(haptic, true)
                        onClose()
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("OK", color = IOSColors.Accent, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.height(24.dp))

        // STYLE
        SectionTitle("Style d'horloge")
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StyleChip("Classique", "classic", clockStyle) { if (hapticEnabled) triggerHapticMove(haptic); onClockStyleChange(it) }
            StyleChip("Capsule", "capsule", clockStyle) { if (hapticEnabled) triggerHapticMove(haptic); onClockStyleChange(it) }
            StyleChip("Verre", "glass", clockStyle) { if (hapticEnabled) triggerHapticMove(haptic); onClockStyleChange(it) }
        }

        Spacer(Modifier.height(20.dp))

        //
