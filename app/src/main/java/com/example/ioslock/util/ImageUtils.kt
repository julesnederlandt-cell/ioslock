package com.example.ioslock.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    fun loadFromUri(context: Context, uri: Uri): Bitmap? = try {
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it)
        }
    } catch (e: Exception) {
        null
    }

    fun loadFromPath(path: String): Bitmap? = try {
        BitmapFactory.decodeFile(path)
    } catch (e: Exception) {
        null
    }

    fun saveWallpaperLocally(context: Context, uri: Uri): String? {
        return try {
            val bmp = loadFromUri(context, uri) ?: return null
            val file = File(context.filesDir, "wallpaper.jpg")
            FileOutputStream(file).use { out ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
