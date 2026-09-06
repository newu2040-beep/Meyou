package com.example.util

import android.content.Context
import java.io.File
import java.text.DecimalFormat

object StorageCacheHelper {

    fun getCacheSizeBytes(context: Context): Long {
        var size = getFolderSize(context.cacheDir)
        context.externalCacheDir?.let {
            size += getFolderSize(it)
        }
        val exportDir = File(context.cacheDir, "exports")
        if (exportDir.exists()) {
            size += getFolderSize(exportDir)
        }
        return size
    }

    fun getDatabaseSizeBytes(context: Context): Long {
        var size = 0L
        val dbFile = context.getDatabasePath("meyou_reading.db")
        if (dbFile.exists()) {
            size += dbFile.length()
        }
        val dbShm = context.getDatabasePath("meyou_reading.db-shm")
        if (dbShm.exists()) {
            size += dbShm.length()
        }
        val dbWal = context.getDatabasePath("meyou_reading.db-wal")
        if (dbWal.exists()) {
            size += dbWal.length()
        }
        return size
    }

    fun clearRecentCache(context: Context): Boolean {
        return try {
            deleteDir(context.cacheDir)
            context.externalCacheDir?.let { deleteDir(it) }
            ImageHelper.clearImageCache()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getFolderSize(file: File?): Long {
        if (file == null || !file.exists()) return 0L
        var size = 0L
        if (file.isDirectory) {
            file.listFiles()?.forEach { child ->
                size += getFolderSize(child)
            }
        } else {
            size = file.length()
        }
        return size
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list() ?: return true
            for (child in children) {
                val success = deleteDir(File(dir, child))
                if (!success) return false
            }
            return dir.delete()
        } else if (dir != null && dir.isFile) {
            return dir.delete()
        }
        return false
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 KB"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
        return DecimalFormat("#,##0.#").format(value) + " " + units[digitGroups]
    }
}
