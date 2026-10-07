package com.example.ioslock.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ioslock.util.DepthEffect
import com.example.ioslock.util.ImageUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EditScreen(
    onCancel: () -> Unit,
    onSave: (wallpaperPath: String, subjectPath: String?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var now by remember { mutableStateOf(Date()) }
    var isProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            now = Date()
            delay(1000)
        }
    }

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { photoUri = it } }

    Box(Modifier.fillMaxSize().background(Color.Black)) {

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
        // HORLOGE
        // ========================================================
        Column(
            Modifier.fillMaxWidth().padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

            // Bouton "Ajouter" — déclenche segmentation + sauvegarde
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
                                // 1. Sauvegarde la photo
                                val wallpaperPath = ImageUtils.saveWallpaperLocally(context, u)

                                // 2. Analyse ML Kit
                                DepthEffect.clearCache(context)
                                val subjectPath = DepthEffect.extractSubject(context, u)

                                // 3. Callback
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
