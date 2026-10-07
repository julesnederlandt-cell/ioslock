package com.example.ioslock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.ioslock.data.WallpaperStore
import com.example.ioslock.ui.LockScreen

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
        setContent {
            val context = LocalContext.current
            val store = remember { WallpaperStore(context) }

            var wallpaperPath by remember { mutableStateOf<String?>(null) }
            var subjectPath by remember { mutableStateOf<String?>(null) }
            var clockFont by remember { mutableStateOf(WallpaperStore.DEFAULT_FONT) }
            var clockColor by remember { mutableStateOf(WallpaperStore.DEFAULT_COLOR) }
            var clockScale by remember { mutableStateOf(WallpaperStore.DEFAULT_SCALE) }
            var clock24h by remember { mutableStateOf(WallpaperStore.DEFAULT_24H == 1) }
            var clockPosY by remember { mutableStateOf(WallpaperStore.DEFAULT_POSITION_Y) }
            var glassIntensity by remember { mutableStateOf(WallpaperStore.DEFAULT_GLASS_INTENSITY) }
            var glassThickness by remember { mutableStateOf(WallpaperStore.DEFAULT_GLASS_THICKNESS) }
            var glassTinted by remember { mutableStateOf(WallpaperStore.DEFAULT_GLASS_TINTED == 1) }
            var hapticEnabled by remember { mutableStateOf(WallpaperStore.DEFAULT_HAPTIC_ENABLED == 1) }
            var clockStyle by remember { mutableStateOf(WallpaperStore.DEFAULT_CLOCK_STYLE) }

            LaunchedEffect(Unit) { store.wallpaperPathFlow.collect { wallpaperPath = it } }
            LaunchedEffect(Unit) { store.subjectPathFlow.collect { subjectPath = it } }
            LaunchedEffect(Unit) { store.clockFontFlow.collect { clockFont = it } }
            LaunchedEffect(Unit) { store.clockColorFlow.collect { clockColor = it } }
            LaunchedEffect(Unit) { store.clockScaleFlow.collect { clockScale = it } }
            LaunchedEffect(Unit) { store.clock24hFlow.collect { clock24h = it } }
            LaunchedEffect(Unit) { store.clockPositionYFlow.collect { clockPosY = it } }
            LaunchedEffect(Unit) { store.glassIntensityFlow.collect { glassIntensity = it } }
            LaunchedEffect(Unit) { store.glassThicknessFlow.collect { glassThickness = it } }
            LaunchedEffect(Unit) { store.glassTintedFlow.collect { glassTinted = it } }
            LaunchedEffect(Unit) { store.hapticEnabledFlow.collect { hapticEnabled = it } }
            LaunchedEffect(Unit) { store.clockStyleFlow.collect { clockStyle = it } }

            LockScreen(
                wallpaperPath = wallpaperPath,
                subjectPath = subjectPath,
                clockFont = clockFont,
                clockColor = clockColor,
                clockScale = clockScale,
                clock24h = clock24h,
                clockPositionY = clockPosY,
                glassIntensity = glassIntensity,
                glassThickness = glassThickness,
                glassTinted = glassTinted,
                hapticEnabled = hapticEnabled,
                clockStyle = clockStyle
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { /* bloqué */ }
}

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
