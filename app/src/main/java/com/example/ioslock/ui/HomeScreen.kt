package com.example.ioslock.ui

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    Box(Modifier.fillMaxSize().background(IOSColors.Background)) {

        // ========================================================
        // COUCHE 1 : FOND PHOTO FLOUTÉ
        // ========================================================
        val bgBitmap = remember(wallpaperPath) {
            wallpaperPath?.let { ImageUtils.loadFromPath(it) }
        }

        if (bgBitmap != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ : vrai flou
            Image(
                bitmap = bgBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(40.dp)
            )
        } else if (bgBitmap != null) {
            // Fallback : pas de flou mais on assombrit
            Image(
                bitmap = bgBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // ========================================================
        // COUCHE 2 : VOILE SOMBRE PAR-DESSUS LA PHOTO
        // ========================================================
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.45f),
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
        )

        // ========================================================
        // COUCHE 3 : CONTENU
        // ========================================================
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(40.dp))

            // Titre style iOS
            Text(
                "Fond d'écran",
                color = IOSColors.TextPrimary,
                fontSize = IOSTypography.LargeTitleSize,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp))

            // ====================================================
            // CARROUSEL DE VIGNETTES
            // ====================================================
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Bouton "+"
                Box(
                    Modifier
                        .size(105.dp, 190.dp)
                        .clip(RoundedCornerShape(IOSDimensions.CornerRadiusLarge))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.18f),
                                    Color.White.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .border(
                            0.5.dp,
                            IOSColors.GlassBorder,
                            RoundedCornerShape(IOSDimensions.CornerRadiusLarge)
                        )
                        .clickableNoRipple { onAddNew() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "+",
                            color = IOSColors.TextPrimary,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Light
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Ajouter",
                            color = IOSColors.TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Vignette du fond actuel
                if (bgBitmap != null) {
                    Box(
                        Modifier
                            .size(105.dp, 190.dp)
                            .clip(RoundedCornerShape(IOSDimensions.CornerRadiusLarge))
                            .border(
                                2.dp,
                                IOSColors.Accent,
                                RoundedCornerShape(IOSDimensions.CornerRadiusLarge)
                            )
                    ) {
                        Image(
                            bitmap = bgBitmap.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ====================================================
            // BOUTONS DU BAS
            // ====================================================
            GlassButton(
                text = "Autoriser l'affichage par-dessus",
                onClick = onRequestOverlay,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            GlassButton(
                text = "Tester l'écran de verrouillage",
                onClick = onTest,
                modifier = Modifier.fillMaxWidth(),
                accent = true
            )

            Spacer(Modifier.height(40.dp))
        }
    }
}
