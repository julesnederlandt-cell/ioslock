package com.example.ioslock.ui

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ioslock.util.ImageUtils
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LockScreen(wallpaperPath: String?) {
    val context = LocalContext.current
    var now by remember { mutableStateOf(Date()) }
    val configuration = LocalConfiguration.current
    val screenHeightPx = with(LocalDensity.current) {
        configuration.screenHeightDp.dp.toPx()
    }

    var dragOffset by remember { mutableStateOf(0f) }
    var isDismissing by remember { mutableStateOf(false) }

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

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .graphicsLayer { translationY = animatedOffset }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (dragOffset < -screenHeightPx * 0.25f) {
                            isDismissing = true
                        } else {
                            dragOffset = 0f
                        }
                    },
                    onDragCancel = { dragOffset = 0f }
                ) { _, dragAmount ->
                    if (!isDismissing) {
                        dragOffset = (dragOffset + dragAmount)
                            .coerceIn(-screenHeightPx, 0f)
                    }
                }
            }
    ) {
        val bmp = remember(wallpaperPath) {
            wallpaperPath?.let { ImageUtils.loadFromPath(it) }
        }
        if (bmp != null) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color(0xFF1C1C2E), Color.Black))
                )
            )
        }

        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))
            Text(
                SimpleDateFormat("EEEE d MMMM", Locale.FRENCH).format(now),
                color = IOSColors.TextPrimary.copy(alpha = 0.9f),
                fontSize = IOSTypography.DateSize,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                SimpleDateFormat("HH:mm", Locale.FRANCE).format(now),
                color = IOSColors.TextPrimary,
                fontSize = IOSTypography.ClockSize,
                fontWeight = FontWeight.Light,
                letterSpacing = IOSTypography.ClockLetterSpacing
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(40.dp))
        }
    }
}
