package com.example.ioslock

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.ioslock.data.WallpaperStore
import com.example.ioslock.ui.EditScreen
import com.example.ioslock.ui.HomeScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { EditorApp() }
    }
}

@Composable
fun EditorApp() {
    val context = LocalContext.current
    val store = remember { WallpaperStore(context) }
    val scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf("home") }
    var wallpaperPath by remember { mutableStateOf<String?>(null) }

    // Charge le wallpaper depuis DataStore
    LaunchedEffect(Unit) {
        store.wallpaperPathFlow.collect { path ->
            wallpaperPath = path
        }
    }

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
                scope.launch { store.setWallpaperPath(path) }
                currentScreen = "home"
            }
        )
    }
}
