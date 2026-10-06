package com.example.ioslock

import android.Manifest
import android.app.*
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

// ============================================================
// PRÉFÉRENCES
// ============================================================
object Prefs {
    private const val NAME = "ios_lock_prefs"
    private const val KEY_WALLPAPER = "wallpaper_path"

    fun prefs(ctx: Context) = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    fun getWallpaperPath(ctx: Context): String? = prefs(ctx).getString(KEY_WALLPAPER, null)
    fun setWallpaperPath(ctx: Context, path: String?) =
        prefs(ctx).edit().putString(KEY_WALLPAPER, path).apply()
}

// ============================================================
// ACTIVITY PRINCIPALE
// ============================================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { EditorApp() }
    }
}

@Composable
fun EditorApp() {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf("home") }
    var wallpaperPath by remember { mutableStateOf(Prefs.getWallpaperPath(context)) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        val perms = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.READ_MEDIA_IMAGES)
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            perms.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permLauncher.launch(perms.toTypedArray())
    }

    when (currentScreen) {
        "home" -> HomeScreen(
            wallpaperPath = wallpaperPath,
            onAddNew = { currentScreen = "edit" },
            onTest = {
                context.startActivity(Intent(context, LockActivity::class.java))
            },
            onRequestOverlay = {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
            }
        )
        "edit" -> EditScreen(
            onCancel = { currentScreen = "home" },
            onSave = { path ->
                Prefs.setWallpaperPath(context, path)
                wallpaperPath = path
                currentScreen = "home"
            }
        )
    }
}

// ============================================================
// ÉCRAN D'ACCUEIL
// ============================================================
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
            .background(Color(0xFF0A0A0F))
            .padding(16.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        Text("Fond d'écran", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))

        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier
                    .size(100.dp, 180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1C1C22))
                    .clickable { onAddNew() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("+", color = Color(0xFF0A84FF), fontSize = 40.sp, fontWeight = FontWeight.Light)
                    Spacer(Modifier.height(4.dp))
                    Text("Ajouter", color = Color(0xFF0A84FF), fontSize = 12.sp)
                }
            }

            wallpaperPath?.let { path ->
                val bmp = remember(path) { BitmapFactory.decodeFile(path) }
                bmp?.let {
                    Box(
                        Modifier
                            .size(100.dp, 180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, Color(0xFF0A84FF), RoundedCornerShape(16.dp))
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
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3A3C)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Autoriser l'affichage par-dessus", color = Color.White, fontSize = 14.sp)
        }
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onTest,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Tester l'écran de verrouillage", color = Color.White, fontSize = 15.sp)
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ============================================================
// ÉCRAN D'ÉDITION
// ============================================================
@Composable
fun EditScreen(onCancel: () -> Unit, onSave: (String) -> Unit) {
    val context = LocalContext.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var now by remember { mutableStateOf(Date()) }

    LaunchedEffect(Unit) {
        while (true) { now = Date(); delay(1000) }
    }

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { photoUri = it } }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        val uri = photoUri
        if (uri != null) {
            val bmp = remember(uri) { loadBitmapFromUri(context, uri) }
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
                    Text("+", color = Color.White.copy(alpha = 0.6f), fontSize = 60.sp)
                    Text("Choisir une photo", color = Color.White.copy(alpha = 0.6f), fontSize = 16.sp)
                }
            }
        }

        Column(
            Modifier.fillMaxWidth().padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                SimpleDateFormat("EEEE d MMMM", Locale.FRENCH).format(now),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                SimpleDateFormat("HH:mm", Locale.FRANCE).format(now),
                color = Color.White,
                fontSize = 90.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-2).sp
            )
        }

        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color(0x99000000), RoundedCornerShape(16.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) {
                Text("Annuler", color = Color.White, fontSize = 15.sp)
            }
            TextButton(onClick = { pickImage.launch("image/*") }) {
                Text("Photo", color = Color(0xFF0A84FF), fontSize = 15.sp)
            }
            Button(
                onClick = {
                    val u = photoUri
                    if (u != null) {
                        val path = saveWallpaperLocally(context, u)
                        if (path != null) onSave(path)
                    }
                },
                enabled = photoUri != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0A84FF),
                    disabledContainerColor = Color(0xFF3A3A3A)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ajouter", color = Color.White, fontSize = 15.sp)
            }
        }
    }
}

// ============================================================
// ÉCRAN DE VERROUILLAGE — SWIPE VERS LE HAUT
// ============================================================
class LockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        setContent { LockScreen() }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { /* bloqué */ }
}

@Composable
fun LockScreen() {
    val context = LocalContext.current
    var now by remember { mutableStateOf(Date()) }
    val configuration = LocalConfiguration.current
    val screenHeightPx = with(androidx.compose.ui.platform.LocalDensity.current) {
        configuration.screenHeightDp.dp.toPx()
    }

    // Position Y de l'écran (0 = repos, -screenHeight = déverrouillé)
    var dragOffset by remember { mutableStateOf(0f) }
    var isDismissing by remember { mutableStateOf(false) }

    val animatedOffset by animateFloatAsState(
        targetValue = if (isDismissing) -screenHeightPx else dragOffset.toFloat(),
        animationSpec = androidx.compose.animation.core.tween(300),
        label = "offset",
        finishedListener = {
            if (isDismissing) (context as? ComponentActivity)?.finish()
        }
    )

    LaunchedEffect(Unit) {
        while (true) { now = Date(); delay(1000) }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .graphicsLayer { translationY = animatedOffset }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        // Si on a swipé assez haut, on déverrouille
                        if (dragOffset < -screenHeightPx * 0.25f) {
                            isDismissing = true
                        } else {
                            // Sinon retour à la position initiale
                            dragOffset = 0f
                        }
                    },
                    onDragCancel = { dragOffset = 0f }
                ) { _, dragAmount ->
                    // dragAmount est négatif quand on va vers le haut
                    if (!isDismissing) {
                        val newOffset = (dragOffset + dragAmount).coerceIn(-screenHeightPx, 0f)
                        dragOffset = newOffset
                    }
                }
            }
    ) {
        // Fond
        val path = remember { Prefs.getWallpaperPath(context) }
        val bmp = path?.let { remember(it) { BitmapFactory.decodeFile(it) } }
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
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                SimpleDateFormat("HH:mm", Locale.FRANCE).format(now),
                color = Color.White,
                fontSize = 90.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-2).sp
            )

            Spacer(Modifier.weight(1f))

            // Indicateur de swipe (petite barre blanche en bas)
            Box(
                Modifier
                    .width(140.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.8f))
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Tirer vers le haut",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

// ============================================================
// RECEIVER
// ============================================================
class ScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_ON,
            Intent.ACTION_USER_PRESENT -> {
                val i = Intent(context, LockActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                context.startActivity(i)
            }
        }
    }
}

// ============================================================
// HELPERS
// ============================================================
fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? = try {
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
} catch (e: Exception) { null }

fun saveWallpaperLocally(context: Context, uri: Uri): String? {
    return try {
        val bmp = loadBitmapFromUri(context, uri) ?: return null
        val file = File(context.filesDir, "wallpaper.jpg")
        FileOutputStream(file).use { out ->
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        file.absolutePath
    } catch (e: Exception) { null }
}
