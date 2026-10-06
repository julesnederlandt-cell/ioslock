package com.example.ioslock

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenter
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        setContent { IOSLockScreen() }
    }
}

@Composable
fun IOSLockScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var foregroundBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    var now by remember { mutableStateOf(Date()) }
    var entered by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    val correctPin = "1234"

    LaunchedEffect(Unit) {
        while (true) { now = Date(); delay(1000) }
    }

    LaunchedEffect(error) {
        if (error) { delay(600); entered = ""; error = false }
    }

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            photoUri = it
            isProcessing = true
            scope.launch {
                foregroundBitmap = withContext(Dispatchers.IO) {
                    segmentSubject(context, it)
                }
                isProcessing = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1C1C2E), Color(0xFF0A0A15), Color.Black)
                )
            )
    ) {
        photoUri?.let { uri ->
            val bmp = remember(uri) { loadBitmap(context, uri) }
            bmp?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (foregroundBitmap != null) Modifier.blur(20.dp) else Modifier
                        )
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().zIndex(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))
            Text(
                text = SimpleDateFormat("EEEE d MMMM", Locale.FRENCH).format(now),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = SimpleDateFormat("HH:mm", Locale.FRANCE).format(now),
                color = Color.White,
                fontSize = 96.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-2).sp
            )
        }

        foregroundBitmap?.let { fg ->
            Image(
                bitmap = fg.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().zIndex(2f)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().zIndex(3f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))

            Text(
                text = if (photoUri == null) "Choisir un fond" else "Changer le fond",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable { pickImage.launch("image/*") }
                    .padding(8.dp)
            )

            Spacer(Modifier.height(24.dp))
            PinDots(entered.length, 4, error)
            Spacer(Modifier.height(24.dp))

            Keypad(
                onDigit = { d ->
                    if (entered.length < 4 && !error) {
                        entered += d
                        if (entered.length == 4 && entered != correctPin) {
                            error = true
                        }
                    }
                },
                onDelete = { if (entered.isNotEmpty()) entered = entered.dropLast(1) }
            )

            Spacer(Modifier.height(40.dp))
        }

        if (isProcessing) {
            Box(
                Modifier.fillMaxSize().zIndex(4f)
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Text("Analyse de l'image...", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}

suspend fun segmentSubject(context: android.content.Context, uri: Uri): Bitmap? {
    return try {
        val inputImage = InputImage.fromFilePath(context, uri)
        val options = SubjectSegmenterOptions.Builder()
            .enableForegroundBitmap()
            .build()
        val segmenter: SubjectSegmenter = SubjectSegmentation.getClient(options)
        val task = segmenter.process(inputImage)
        val result = withContext(Dispatchers.IO) {
            com.google.android.gms.tasks.Tasks.await(task)
        }
        val fgBitmap = result.foregroundBitmap
        segmenter.close()
        fgBitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun loadBitmap(context: android.content.Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            android.graphics.BitmapFactory.decodeStream(input)
        }
    } catch (e: Exception) { null }
}

@Composable
fun PinDots(count: Int, total: Int, isError: Boolean) {
    val color by animateColorAsState(
        if (isError) Color(0xFFFF453A) else Color.White
    )
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        repeat(total) { i ->
            val filled = i < count
            val scale by animateFloatAsState(
                if (filled) 1f else 0.6f,
                spring(dampingRatio = 0.5f, stiffness = 500f),
                label = "dot"
            )
            Box(
                Modifier
                    .size(16.dp)
                    .scale(scale)
                    .background(if (filled) color else Color.Transparent, CircleShape)
                    .border(1.5.dp, color.copy(alpha = if (filled) 1f else 0.6f), CircleShape)
            )
        }
    }
}

@Composable
fun Keypad(onDigit: (String) -> Unit, onDelete: () -> Unit) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "⌫")
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                row.forEach { key ->
                    if (key.isEmpty()) Spacer(Modifier.size(76.dp))
                    else GlassKey(key, key == "⌫") {
                        if (key == "⌫") onDelete() else onDigit(key)
                    }
                }
            }
        }
    }
}

@Composable
fun GlassKey(label: String, isDelete: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.10f)
                    )
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isDelete) {
            Text("⌫", color = Color.White, fontSize = 26.sp)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Light)
                val subs = mapOf(
                    "2" to "ABC", "3" to "DEF", "4" to "GHI", "5" to "JKL",
                    "6" to "MNO", "7" to "PQRS", "8" to "TUV", "9" to "WXYZ"
                )
                subs[label]?.let {
                    Text(it, color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp, letterSpacing = 2.sp)
                }
            }
        }
    }
}
