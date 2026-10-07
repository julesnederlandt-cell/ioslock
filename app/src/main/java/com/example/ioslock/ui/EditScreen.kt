package com.example.ioslock.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ioslock.util.DepthEffect
import com.example.ioslock.util.ImageUtils
import io.github.nadeemiqbal.liquidglass.liquidGlassSource
import io.github.nadeemiqbal.liquidglass.rememberLiquidGlassState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.abs

@Composable
fun EditScreen(
    onCancel: () -> Unit,
    onSave: (
        wallpaperPath: String,
        subjectPath: String?,
        font: String,
        color: String,
        scale: Float,
        is24h: Boolean,
        posY: Float,
        glassIntensity: Float,
        glassThickness: Float,
        glassTinted: Boolean,
        hapticEnabled: Boolean,
        clockStyle: String
    ) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    // State partagé pour Liquid Glass
    val glassState = rememberLiquidGlassState()

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var now by remember { mutableStateOf(Date()) }
    var isProcessing by remember { mutableStateOf(false) }

    var clockFont by remember { mutableStateOf("default") }
    var clockColor by remember { mutableStateOf("white") }
    var clockScale by remember { mutableStateOf(1.0f) }
    var clock24h by remember { mutableStateOf(true) }
    var clockPosY by remember { mutableStateOf(0.15f) }
    var showClockPanel by remember { mutableStateOf(false) }
    var clockStyle by remember { mutableStateOf("capsule") }

    var glassIntensity by remember { mutableStateOf(0.6f) }
    var glassThickness by remember { mutableStateOf(1.0f) }
    var glassTinted by remember { mutableStateOf(false) }
    var hapticEnabled by remember { mutableStateOf(true) }

    var isResizing by remember { mutableStateOf(false) }
    var screenHeightPx by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            now = Date()
            delay(1000)
        }
    }

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { photoUri = it } }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { screenHeightPx = it.height.toFloat() }
    ) {

        // ====================================================
        // PHOTO DE FOND — liquidGlassSource pour que le verre la capte
        // ====================================================
        val uri = photoUri
        if (uri != null) {
            val bmp = remember(uri) { ImageUtils.loadFromUri(context, uri) }
            bmp?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .liquidGlassSource(glassState)
                )
            }
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF2C2C3E), Color(0xFF0A0A15))
                        )
                    )
                    .clickable { pickImage.launch("image/*") }
                    .liquidGlassSource(glassState),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("+", color = IOSColors.TextTertiary, fontSize = 60.sp)
                    Text("Choisir une photo", color = IOSColors.TextTertiary, fontSize = 16.sp)
                }
            }
        }

        // ====================================================
        // HORLOGE AVEC GLASSCARD
        // ====================================================
        val clockY = if (screenHeightPx > 0f) {
            (screenHeightPx * clockPosY).coerceIn(
                with(density) { 60.dp.toPx() },
                screenHeightPx - with(density) { 350.dp.toPx() }
            )
        } else {
            with(density) { 80.dp.toPx() }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .absoluteOffset(y = with(density) { clockY.toDp() }),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                Modifier
                    .wrapContentSize()
                    .pointerInput(screenHeightPx) {
                        detectDragGestures(
                            onDragStart = { if (hapticEnabled) triggerHapticLongPress(haptic) }
                        ) { change, dragAmount ->
                            change.consume()
                            if (screenHeightPx > 0f && !isResizing) {
                                val newY = (clockY + dragAmount.y)
                                    .coerceIn(
                                        with(density) { 60.dp.toPx() },
                                        screenHeightPx - with(density) { 350.dp.toPx() }
                                    )
                                clockPosY = newY / screenHeightPx
                                if (hapticEnabled && abs(dragAmount.y) > 4f) triggerHapticMove(haptic)
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                if (hapticEnabled) triggerHapticLongPress(haptic)
                                showClockPanel = true
                            }
                        )
                    }
            ) {
                Box {
                    LiquidClock(
                        date = now,
                        fontKey = clockFont,
                        colorKey = clockColor,
                        scale = clockScale,
                        is24h = clock24h,
                        glassState = glassState,
                        style = clockStyle,
                        glassIntensity = glassIntensity,
                        hapticEnabled = false,
                        tinted = glassTinted
                    )

                    // Resize handle (bas centre)
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 14.dp)
                            .size(24.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(if (isResizing) Color.White else Color.White.copy(alpha = 0.85f))
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = {
                                        isResizing = true
                                        if (hapticEnabled) triggerHapticLongPress(haptic)
                                    },
                                    onDragEnd = { isResizing = false },
                                    onDragCancel = { isResizing = false }
                                ) { change, dragAmount ->
                                    change.consume()
                                    val delta = (dragAmount.x + dragAmount.y) / 350f
                                    val newScale = (clockScale + delta).coerceIn(0.6f, 1.5f)
                                    if (newScale != clockScale) {
                                        clockScale = newScale
                                        if (hapticEnabled && abs(delta) > 0.005f) triggerHapticMove(haptic)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier
                                .size(6.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        )
                    }
                }
            }
        }

        // ====================================================
        // BARRE DU BAS
        // ====================================================
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(IOSDimensions.Padding)
                .background(IOSColors.GlassDark, RoundedCornerShape(IOSDimensions.CornerRadius))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel, enabled = !isProcessing) {
                Text("Annuler", color = IOSColors.TextPrimary, fontSize = IOSTypography.BodySize)
            }
            TextButton(onClick = { pickImage.launch("image/*") }, enabled = !isProcessing) {
                Text("Photo", color = IOSColors.Accent, fontSize = IOSTypography.BodySize)
            }
            Box(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (photoUri != null && !isProcessing) IOSColors.Accent else IOSColors.AccentSecondary)
                    .clickableNoRipple {
                        val u = photoUri
                        if (u != null && !isProcessing) {
                            isProcessing = true
                            scope.launch {
                                val wallpaperPath = ImageUtils.saveWallpaperLocally(context, u)
                                DepthEffect.clearCache(context)
                                val subjectPath = DepthEffect.extractSubject(context, u)
                                if (wallpaperPath != null) {
                                    onSave(
                                        wallpaperPath, subjectPath, clockFont, clockColor,
                                        clockScale, clock24h, clockPosY, glassIntensity,
                                        glassThickness, glassTinted, hapticEnabled, clockStyle
                                    )
                                }
                                isProcessing = false
                            }
                        }
                    }
                    .padding(vertical = 12.dp, horizontal = 24.dp)
            ) {
                Text("Ajouter", color = IOSColors.TextPrimary, fontSize = IOSTypography.BodySize, fontWeight = FontWeight.Medium)
            }
        }

        // PANNEAU
        if (showClockPanel) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable {
                        if (hapticEnabled) triggerHapticToggle(haptic, false)
                        showClockPanel = false
                    }
            ) {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .clickable(enabled = false) { }
                ) {
                    ClockCustomizationPanel(
                        fontKey = clockFont,
                        colorKey = clockColor,
                        scale = clockScale,
                        is24h = clock24h,
                        glassIntensity = glassIntensity,
                        glassThickness = glassThickness,
                        glassTinted = glassTinted,
                        hapticEnabled = hapticEnabled,
                        clockStyle = clockStyle,
                        onFontChange = { clockFont = it },
                        onColorChange = { clockColor = it },
                        onScaleChange = { clockScale = it },
                        on24hChange = { clock24h = it },
                        onGlassIntensityChange = { glassIntensity = it },
                        onGlassThicknessChange = { glassThickness = it },
                        onGlassTintedChange = { glassTinted = it },
                        onHapticEnabledChange = { hapticEnabled = it },
                        onClockStyleChange = { clockStyle = it },
                        onClose = { showClockPanel = false }
                    )
                }
            }
        }

        // SPINNER
        if (isProcessing) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = IOSColors.Accent)
                    Spacer(Modifier.height(16.dp))
                    Text("Analyse de l'image...", color = IOSColors.TextPrimary, fontSize = 15.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Détection du sujet en cours", color = IOSColors.TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}
