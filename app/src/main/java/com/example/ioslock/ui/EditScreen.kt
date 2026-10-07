package com.example.ioslock.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ioslock.util.DepthEffect
import com.example.ioslock.util.ImageUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun EditScreen(
    onCancel: () -> Unit,
    onSave: (wallpaperPath: String, subjectPath: String?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var now by remember { mutableStateOf(Date()) }
    var isProcessing by remember { mutableStateOf(false) }

    // État de personnalisation de l'horloge
    var clockFont by remember { mutableStateOf("default") }
    var clockColor by remember { mutableStateOf("white") }
    var clockScale by remember { mutableStateOf(1.0f) }
    var clock24h by remember { mutableStateOf(true) }
    var clockPosY by remember { mutableStateOf(0.15f) }
    var showClockPanel by remember { mutableStateOf(false) }

    // Hauteur réelle de l'écran en pixels
    var screenHeightPx by remember { mutableStateOf(0f) }
    val screenHeightDp = configuration.screenHeightDp.dp

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
            .onSizeChanged { size ->
                screenHeightPx = size.height.toFloat()
            }
    ) {

        // ========================================================
        // APERÇU PHOTO
        // ========================================================
        val uri = photoUri
        if (uri != null) {
            val bmp = remember(uri) { ImageUtils.loadFromUri(context, uri) }
            bmp?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
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
                    .clickable { pickImage.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("+", color = IOSColors.TextTertiary, fontSize = 60.sp)
                    Text(
                        "Choisir une photo",
                        color = IOSColors.TextTertiary,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // ========================================================
        // HORLOGE DRAGGABLE
        // ========================================================
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
                .absoluteOffset(y = with(density) { clockY.toDp() })
                .pointerInput(screenHeightPx) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        if (screenHeightPx > 0f) {
                            val newY = (clockY + dragAmount.y)
                                .coerceIn(
                                    with(density) { 60.dp.toPx() },
                                    screenHeightPx - with(density) { 350.dp.toPx() }
                                )
                            clockPosY = newY / screenHeightPx
                        }
                    }
                },
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { showClockPanel = true }
                    .padding(16.dp)
            ) {
                ClockText(
                    date = now,
                    fontKey = clockFont,
                    colorKey = clockColor,
                    scale = clockScale,
                    is24h = clock24h
                )
            }
        }

        // ========================================================
        // BARRE DU BAS
        // ========================================================
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
                    .background(
                        if (photoUri != null && !isProcessing) IOSColors.Accent
                        else IOSColors.AccentSecondary
                    )
                    .clickableNoRipple {
                        val u = photoUri
                        if (u != null && !isProcessing) {
                            isProcessing = true
                            scope.launch {
                                val wallpaperPath = ImageUtils.saveWallpaperLocally(context, u)
                                DepthEffect.clearCache(context)
                                val subjectPath = DepthEffect.extractSubject(context, u)

                                if (wallpaperPath != null) {
                                    onSave(wallpaperPath, subjectPath)
                                }
                                isProcessing = false
                            }
                        }
                    }
                    .padding(vertical = 12.dp, horizontal = 24.dp)
            ) {
                Text(
                    "Ajouter",
                    color = IOSColors.TextPrimary,
                    fontSize = IOSTypography.BodySize,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // ========================================================
        // PANNEAU DE PERSONNALISATION DE L'HORLOGE
        // ========================================================
        if (showClockPanel) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { showClockPanel = false }
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
                        onFontChange = { clockFont = it },
                        onColorChange = { clockColor = it },
                        onScaleChange = { clockScale = it },
                        on24hChange = { clock24h = it },
                        onClose = { showClockPanel = false }
                    )
                }
            }
        }

        // ========================================================
        // SPINNER DE TRAITEMENT
        // ========================================================
        if (isProcessing) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = IOSColors.Accent)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Analyse de l'image...",
                        color = IOSColors.TextPrimary,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Détection du sujet en cours",
                        color = IOSColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
