package com.example.ioslock.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "ios_lock_settings")

class WallpaperStore(private val context: Context) {

    companion object {
        private val KEY_WALLPAPER = stringPreferencesKey("wallpaper_path")
    }

    val wallpaperPathFlow: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[KEY_WALLPAPER] }

    suspend fun setWallpaperPath(path: String?) {
        context.dataStore.edit { prefs ->
            if (path == null) prefs.remove(KEY_WALLPAPER)
            else prefs[KEY_WALLPAPER] = path
        }
    }
}
