package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageHelper {

    // Cache up to 20 thumbnail bitmaps in memory
    private val memoryCache: LruCache<String, Bitmap> = object : LruCache<String, Bitmap>(20) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return 1
        }
    }

    suspend fun saveCoverFromUri(context: Context, sourceUri: Uri, prefix: String = "cover"): String? {
        return withContext(Dispatchers.IO) {
            try {
                val coversDir = File(context.filesDir, "covers").apply { mkdirs() }
                val targetFile = File(coversDir, "${prefix}_${System.currentTimeMillis()}.jpg")

                // Step 1: Decode bounds to calculate sample size
                var inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(inputStream, null, options)
                inputStream?.close()

                val reqWidth = 600
                val reqHeight = 900
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
                options.inJustDecodeBounds = false
                options.inPreferredConfig = Bitmap.Config.RGB_565 // Use 16-bit to save 50% RAM

                // Step 2: Decode downsampled bitmap
                inputStream = context.contentResolver.openInputStream(sourceUri)
                val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
                inputStream?.close()

                if (bitmap != null) {
                    FileOutputStream(targetFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    }
                    bitmap.recycle()
                    targetFile.absolutePath
                } else {
                    null
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                null
            }
        }
    }

    fun loadSafeThumbnailBitmap(context: Context, pathOrUri: String?, maxDim: Int = 300): Bitmap? {
        if (pathOrUri.isNullOrBlank()) return null

        val cached = memoryCache.get(pathOrUri)
        if (cached != null && !cached.isRecycled) {
            return cached
        }

        return try {
            val file = File(pathOrUri)
            val isLocalFile = file.exists()

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            if (isLocalFile) {
                BitmapFactory.decodeFile(file.absolutePath, options)
            } else {
                val uri = Uri.parse(pathOrUri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)
                }
            }

            options.inSampleSize = calculateInSampleSize(options, maxDim, maxDim)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565

            val bitmap = if (isLocalFile) {
                BitmapFactory.decodeFile(file.absolutePath, options)
            } else {
                val uri = Uri.parse(pathOrUri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)
                }
            }

            if (bitmap != null) {
                memoryCache.put(pathOrUri, bitmap)
            }
            bitmap
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    fun clearImageCache() {
        try {
            memoryCache.evictAll()
        } catch (t: Throwable) {
            // ignore
        }
    }
}
