package com.example.ioslock.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ioslock.util.ImageUtils

@Composable
fun HomeScreen(
    wallpaperPath: String?,
    onAddNew: () -> Unit,
    onTest: () -> Unit,
    onRequestOverlay: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(IOSColors.Background)
            .padding(IOSDimensions.Padding)
    ) {
        Spacer(Modifier.height(20.dp))
        Text(
            "Fond d'écran",
            color = IOSColors.TextPrimary,
            fontSize = IOSTypography.TitleSize,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(20.dp))

        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Bouton "+"
            Box(
                Modifier
                    .size(100.dp, 180.dp)
                    .clip(RoundedCornerShape(IOSDimensions.CornerRadius))
                    .background(IOSColors.Surface)
                    .clickable { onAddNew() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "+",
                        color = IOSColors.Accent,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Light
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Ajouter", color = IOSColors.Accent, fontSize = 12.sp)
                }
            }

            // Vignette du fond actuel
            wallpaperPath?.let { path ->
                val bmp = remember(path) { ImageUtils.loadFromPath(path) }
                bmp?.let {
                    Box(
                        Modifier
                            .size(100.dp, 180.dp)
                            .clip(RoundedCornerShape(IOSDimensions.CornerRadius))
                            .border(
                                2.dp,
                                IOSColors.Accent,
                                RoundedCornerShape(IOSDimensions.CornerRadius)
                            )
                    ) {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onRequestOverlay,
            modifier = Modifier.fillMaxWidth().height(IOSDimensions.ButtonHeight),
            colors = ButtonDefaults.buttonColors(containerColor = IOSColors.AccentSecondary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Autoriser l'affichage par-dessus",
                color = IOSColors.TextPrimary,
                fontSize = 14.sp
            )
        }
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onTest,
            modifier = Modifier.fillMaxWidth().height(IOSDimensions.ButtonHeight),
            colors = ButtonDefaults.buttonColors(containerColor = IOSColors.Accent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Tester l'écran de verrouillage",
                color = IOSColors.TextPrimary,
                fontSize = IOSTypography.BodySize
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}
