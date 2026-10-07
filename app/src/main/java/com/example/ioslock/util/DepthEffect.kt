package com.example.ioslock.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object DepthEffect {

    /**
     * Analyse une image, découpe le sujet principal (personne/objet)
     * et sauvegarde le résultat dans un PNG transparent en local.
     *
     * Retourne le chemin du PNG ou null si la segmentation échoue.
     */
    suspend fun extractSubject(context: Context, uri: Uri): String? {
        return try {
            val inputImage = InputImage.fromFilePath(context, uri)
            val options = SubjectSegmenterOptions.Builder()
                .enableForegroundBitmap()
                .build()
            val segmenter = SubjectSegmentation.getClient(options)

            val task = segmenter.process(inputImage)
            val result = withContext(Dispatchers.IO) {
                Tasks.await(task)
            }

            val fg: Bitmap? = result.foregroundBitmap
            segmenter.close()

            if (fg == null) return null

            // Sauvegarde le PNG
            val file = File(context.filesDir, "subject.png")
            FileOutputStream(file).use { out ->
                fg.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Supprime le cache du sujet (à appeler si on change de photo).
     */
    fun clearCache(context: Context) {
        try {
            File(context.filesDir, "subject.png").delete()
        } catch (_: Exception) { }
    }

    /**
     * Vérifie si un sujet est déjà en cache.
     */
    fun hasCachedSubject(context: Context): Boolean {
        return File(context.filesDir, "subject.png").exists()
    }

    fun getCachedSubjectPath(context: Context): String? {
        val f = File(context.filesDir, "subject.png")
        return if (f.exists()) f.absolutePath else null
    }
}
