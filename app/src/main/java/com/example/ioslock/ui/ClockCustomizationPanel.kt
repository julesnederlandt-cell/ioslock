package com.example.ioslock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ClockCustomizationPanel(
    fontKey: String,
    colorKey: String,
    scale: Float,
    is24h: Boolean,
    onFontChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onScaleChange: (Float) -> Unit,
    on24hChange: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color(0xEE1C1C22))
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Poignée visuelle
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.3f))
        )
        Spacer(Modifier.height(16.dp))

        // Titre + OK
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Personnaliser l'horloge",
                color = IOSColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .clickableNoRipple(onClose)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    "OK",
                    color = IOSColors.Accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ========================================================
        // POLICE
        // ========================================================
        SectionTitle("Police")
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FontChip("Défaut", "default", fontKey, onFontChange)
            FontChip("Serif", "serif", fontKey, onFontChange)
            FontChip("Mono", "mono", fontKey, onFontChange)
            FontChip("Cursive", "cursive", fontKey, onFontChange)
            FontChip("Light", "light", fontKey, onFontChange)
        }

        Spacer(Modifier.height(20.dp))

        // ========================================================
        // COULEUR
        // ========================================================
        SectionTitle("Couleur")
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ColorChip("white", colorKey, onColorChange)
            ColorChip("black", colorKey, onColorChange)
            ColorChip("blue", colorKey, onColorChange)
            ColorChip("yellow", colorKey, onColorChange)
            ColorChip("pink", colorKey, onColorChange)
            ColorChip("green", colorKey, onColorChange)
            ColorChip("orange", colorKey, onColorChange)
        }

        Spacer(Modifier.height(20.dp))

        // ========================================================
        // TAILLE
        // ========================================================
        SectionTitle("Taille")
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("A", color = IOSColors.TextSecondary, fontSize = 14.sp)
            Slider(
                value = scale,
                onValueChange = onScaleChange,
                valueRange = 0.6f..1.5f,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = IOSColors.Accent,
                    inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                )
            )
            Text("A", color = IOSColors.TextSecondary, fontSize = 24.sp)
        }

        Spacer(Modifier.height(16.dp))

        // ========================================================
        // FORMAT 24H
        // ========================================================
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Format 24 heures",
                color = IOSColors.TextPrimary,
                fontSize = 15.sp
            )
            Switch(
                checked = is24h,
                onCheckedChange = on24hChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = IOSColors.Accent,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.White.copy(alpha = 0.15f)
                )
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ============================================================
// SOUS-COMPOSANTS
// ============================================================

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        color = IOSColors.TextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun FontChip(
    label: String,
    key: String,
    selectedKey: String,
    onClick: (String) -> Unit
) {
    val selected = key == selectedKey
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) IOSColors.Accent else Color.White.copy(alpha = 0.1f)
            )
            .border(
                0.5.dp,
                if (selected) Color.Transparent else Color.White.copy(alpha = 0.2f),
                RoundedCornerShape(50)
            )
            .clickableNoRipple { onClick(key) }
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            label,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontFamily = clockFontFromKey(key)
        )
    }
}

@Composable
private fun ColorChip(
    key: String,
    selectedKey: String,
    onClick: (String) -> Unit
) {
    val color = clockColorFromKey(key)
    val selected = key == selectedKey
    Box(
        Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) IOSColors.Accent else Color.White.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickableNoRipple { onClick(key) }
    )
}
