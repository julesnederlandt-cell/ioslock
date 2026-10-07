package com.example.ioslock.ui

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.example.ioslock.util.ImageUtils
import kotlinx.coroutines.delay
import java.util.Date
import kotlin.math.abs

@Composable
fun LockScreen(
    wallpaperPath: String?,
    subjectPath: String?,
    clockFont: String,
    clockColor: String,
    clockScale: Float,
    clock24h: Boolean,
    clockPositionY: Float,
    glassIntensity: Float,
    glassThickness: Float,
    glassTinted: Boolean,
    hapticEnabled: Boolean,
    clockStyle: String
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var now by remember { mutableStateOf(Date()) }
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    var dragOffset by remember { mutableStateOf(0f) }
    var isDismissing by remember { mutableStateOf(false) }
    var lastHapticTime by remember { mutableStateOf(0L) }

    val animatedOffset by animateFloatAsState(
        targetValue = if (isDismissing) -screenHeightPx else dragOffset,
        animationSpec = tween(300),
        label = "offset",
        finishedListener = {
            if (isDismissing) (context as? ComponentActivity)?.finish()
        }
    )

    LaunchedEffect(Unit) {
        while (true) {
            now = Date()
            delay(1000)
        }
    }

    val wallpaperBmp = remember(wallpaperPath) { wallpaperPath?.let { ImageUtils.loadFromPath(it) } }
    val subjectBmp = remember(subjectPath) { subjectPath?.let { ImageUtils.loadFromPath(it) } }
    val hasDepth = subjectBmp != null

    var actualHeightPx by remember { mutableStateOf(0f) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { actualHeightPx = it.height.toFloat() }
            .graphicsLayer { translationY = animatedOffset }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (dragOffset < -screenHeightPx * 0.25f) {
                            if (hapticEnabled) triggerHapticToggle(haptic, true)
                            isDismissing = true
                        } else {
                            dragOffset = 0f
                        }
                    },
                    onDragCancel = { dragOffset = 0f }
                ) { _, dragAmount ->
                    if (!isDismissing) {
                        dragOffset = (dragOffset + dragAmount).coerceIn(-screenHeightPx, 0f)
                        if (hapticEnabled && abs(dragAmount) > 3f) {
                            val t = System.currentTimeMillis()
                            if (t - lastHapticTime > 80) {
                                triggerHapticMove(haptic)
                                lastHapticTime = t
                            }
                        }
                    }
                }
            }
    ) {

        // FOND
        if (wallpaperBmp != null) {
            if (hasDepth && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Image(
                    bitmap = wallpaperBmp.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().blur(20.dp)
                )
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.25f)))
            } else {
                Image(
                    bitmap = wallpaperBmp.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color(0xFF1C1C2E), Color.Black))
                )
            )
        }

        // HORLOGE
        val clockY = if (actualHeightPx > 0f) {
            (actualHeightPx * clockPositionY).coerceIn(
                with(density) { 60.dp.toPx() },
                actualHeightPx - with(density) { 350.dp.toPx() }
            )
        } else {
            with(density) { 80.dp.toPx() }
        }

        Box(
            Modifier.fillMaxWidth().absoluteOffset(y = with(density) { clockY.toDp() }),
            contentAlignment = Alignment.TopCenter
        ) {
            LiquidClock(
                date = now,
                fontKey = clockFont,
                colorKey = clockColor,
                scale = clockScale,
                is24h = clock24h,
                style = clockStyle,
                glassIntensity = glassIntensity,
                glassThickness = glassThickness,
                hapticEnabled = false,
                tinted = glassTinted
            )
        }

        // SUJET (depth effect)
        if (subjectBmp != null) {
            Image(
                bitmap = subjectBmp.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
