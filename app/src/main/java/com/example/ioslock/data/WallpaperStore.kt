package com.example.ioslock.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "ios_lock_settings")

class WallpaperStore(private val context: Context) {

    companion object {
        private val KEY_WALLPAPER = stringPreferencesKey("wallpaper_path")
        private val KEY_SUBJECT = stringPreferencesKey("subject_path")
        private val KEY_CLOCK_FONT = stringPreferencesKey("clock_font")
        private val KEY_CLOCK_COLOR = stringPreferencesKey("clock_color")
        private val KEY_CLOCK_SCALE = floatPreferencesKey("clock_scale")
        private val KEY_CLOCK_POSITION_Y = floatPreferencesKey("clock_pos_y")
        private val KEY_CLOCK_24H = intPreferencesKey("clock_24h")

        // Nouveaux réglages Liquid Glass
        private val KEY_GLASS_INTENSITY = floatPreferencesKey("glass_intensity")
        private val KEY_GLASS_THICKNESS = floatPreferencesKey("glass_thickness")
        private val KEY_GLASS_TINTED = intPreferencesKey("glass_tinted")
        private val KEY_HAPTIC_ENABLED = intPreferencesKey("haptic_enabled")

        // Valeurs par défaut
        const val DEFAULT_FONT = "default"
        const val DEFAULT_COLOR = "white"
        const val DEFAULT_SCALE = 1.0f
        const val DEFAULT_POSITION_Y = 0.15f
        const val DEFAULT_24H = 1
        const val DEFAULT_GLASS_INTENSITY = 0.6f
        const val DEFAULT_GLASS_THICKNESS = 1.0f
        const val DEFAULT_GLASS_TINTED = 0
        const val DEFAULT_HAPTIC_ENABLED = 1
    }

    // ---------- Wallpaper ----------
    val wallpaperPathFlow: Flow<String?> = context.dataStore.data.map { it[KEY_WALLPAPER] }
    suspend fun setWallpaperPath(path: String?) {
        context.dataStore.edit { if (path == null) it.remove(KEY_WALLPAPER) else it[KEY_WALLPAPER] = path }
    }

    // ---------- Sujet (Depth) ----------
    val subjectPathFlow: Flow<String?> = context.dataStore.data.map { it[KEY_SUBJECT] }
    suspend fun setSubjectPath(path: String?) {
        context.dataStore.edit { if (path == null) it.remove(KEY_SUBJECT) else it[KEY_SUBJECT] = path }
    }

    // ---------- Horloge : police ----------
    val clockFontFlow: Flow<String> = context.dataStore.data.map { it[KEY_CLOCK_FONT] ?: DEFAULT_FONT }
    suspend fun setClockFont(value: String) {
        context.dataStore.edit { it[KEY_CLOCK_FONT] = value }
    }

    // ---------- Horloge : couleur ----------
    val clockColorFlow: Flow<String> = context.dataStore.data.map { it[KEY_CLOCK_COLOR] ?: DEFAULT_COLOR }
    suspend fun setClockColor(value: String) {
        context.dataStore.edit { it[KEY_CLOCK_COLOR] = value }
    }

    // ---------- Horloge : taille ----------
    val clockScaleFlow: Flow<Float> = context.dataStore.data.map { it[KEY_CLOCK_SCALE] ?: DEFAULT_SCALE }
    suspend fun setClockScale(value: Float) {
        context.dataStore.edit { it[KEY_CLOCK_SCALE] = value }
    }

    // ---------- Horloge : position Y ----------
    val clockPositionYFlow: Flow<Float> = context.dataStore.data.map { it[KEY_CLOCK_POSITION_Y] ?: DEFAULT_POSITION_Y }
    suspend fun setClockPositionY(value: Float) {
        context.dataStore.edit { it[KEY_CLOCK_POSITION_Y] = value }
    }

    // ---------- Horloge : 24h ----------
    val clock24hFlow: Flow<Boolean> = context.dataStore.data.map { (it[KEY_CLOCK_24H] ?: DEFAULT_24H) == 1 }
    suspend fun setClock24h(value: Boolean) {
        context.dataStore.edit { it[KEY_CLOCK_24H] = if (value) 1 else 0 }
    }

    // ---------- Liquid Glass : intensité ----------
    val glassIntensityFlow: Flow<Float> = context.dataStore.data.map { it[KEY_GLASS_INTENSITY] ?: DEFAULT_GLASS_INTENSITY }
    suspend fun setGlassIntensity(value: Float) {
        context.dataStore.edit { it[KEY_GLASS_INTENSITY] = value }
    }

    // ---------- Liquid Glass : épaisseur ----------
    val glassThicknessFlow: Flow<Float> = context.dataStore.data.map { it[KEY_GLASS_THICKNESS] ?: DEFAULT_GLASS_THICKNESS }
    suspend fun setGlassThickness(value: Float) {
        context.dataStore.edit { it[KEY_GLASS_THICKNESS] = value }
    }

    // ---------- Liquid Glass : teinté ----------
    val glassTintedFlow: Flow<Boolean> = context.dataStore.data.map { (it[KEY_GLASS_TINTED] ?: DEFAULT_GLASS_TINTED) == 1 }
    suspend fun setGlassTinted(value: Boolean) {
        context.dataStore.edit { it[KEY_GLASS_TINTED] = if (value) 1 else 0 }
    }

    // ---------- Haptique ----------
    val hapticEnabledFlow: Flow<Boolean> = context.dataStore.data.map { (it[KEY_HAPTIC_ENABLED] ?: DEFAULT_HAPTIC_ENABLED) == 1 }
    suspend fun setHapticEnabled(value: Boolean) {
        context.dataStore.edit { it[KEY_HAPTIC_ENABLED] = if (value) 1 else 0 }
    }
}
