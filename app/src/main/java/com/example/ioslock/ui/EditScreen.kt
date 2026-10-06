package com.example.ioslock.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ioslock.util.ImageUtils
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EditScreen(onCancel: () -> Unit, onSave: (String) -> Unit) {
    val context = LocalContext.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var now by remember { mutableStateOf(Date()) }

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
            TextButton(onClick = onCancel) {
                Text("Annuler", color = IOSColors.TextPrimary, fontSize = IOSTypography.BodySize)
            }
            TextButton(onClick = { pickImage.launch("image/*") }) {
                Text("Photo", color = IOSColors.Accent, fontSize = IOSTypography.BodySize)
            }
            Button(
                onClick = {
                    val u = photoUri
                    if (u != null) {
                        val path = ImageUtils.saveWallpaperLocally(context, u)
                        if (path != null) onSave(path)
                    }
                },
                enabled = photoUri != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = IOSColors.Accent,
                    disabledContainerColor = IOSColors.AccentSecondary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ajouter", color = IOSColors.TextPrimary, fontSize = IOSTypography.BodySize)
            }
        }
    }
}
