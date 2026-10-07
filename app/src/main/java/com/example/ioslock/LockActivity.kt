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

            LaunchedEffect(Unit) {
                store.wallpaperPathFlow.collect { wallpaperPath = it }
            }
            LaunchedEffect(Unit) {
                store.subjectPathFlow.collect { subjectPath = it }
            }

            LockScreen(
                wallpaperPath = wallpaperPath,
                subjectPath = subjectPath
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
