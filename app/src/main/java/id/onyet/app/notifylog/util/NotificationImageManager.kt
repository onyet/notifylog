package id.onyet.app.notifylog.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Manages saving and deleting bitmap images captured from notification extras
 * (e.g. EXTRA_PICTURE from BigPictureStyle, EXTRA_LARGE_ICON).
 *
 * Images are stored in: [Context.filesDir]/notification_images/<filename>.jpg
 * The directory lives in internal storage — no permissions needed and auto-cleaned on uninstall.
 */
object NotificationImageManager {

    private const val IMAGE_DIR = "notification_images"
    private const val MAX_IMAGE_WIDTH_PX = 800
    private const val JPEG_QUALITY = 80

    /**
     * Saves a [Bitmap] to internal storage and returns the absolute file path.
     * Returns null if the operation fails (e.g. storage full, bitmap recycled).
     *
     * @param context  Application context.
     * @param bitmap   The bitmap to save.
     * @param filename Unique filename without extension (e.g. "notif_1708123456789").
     */
    fun saveBitmap(context: Context, bitmap: Bitmap, filename: String): String? {
        if (bitmap.isRecycled) return null

        return try {
            val dir = getImageDir(context)
            val file = File(dir, "$filename.jpg")

            val scaled = scaleBitmapIfNeeded(bitmap)
            FileOutputStream(file).use { out ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    scaled.compress(Bitmap.CompressFormat.WEBP_LOSSY, JPEG_QUALITY, out)
                } else {
                    @Suppress("DEPRECATION")
                    scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
                }
                out.flush()
            }
            // Recycle the scaled copy only if it's a new object
            if (scaled !== bitmap) scaled.recycle()

            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Deletes a single image file by its absolute path.
     * Safe to call with a null or non-existent path.
     */
    fun deleteImage(imagePath: String?) {
        if (imagePath.isNullOrBlank()) return
        try {
            val file = File(imagePath)
            if (file.exists()) file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Deletes all saved notification images (used by "Delete All" operations).
     */
    fun deleteAllImages(context: Context) {
        try {
            val dir = getImageDir(context)
            if (dir.exists()) {
                dir.listFiles()?.forEach { it.delete() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Deletes image files for the given list of paths.
     * Useful when bulk-deleting filtered/older notifications.
     */
    fun deleteImages(imagePaths: List<String?>) {
        imagePaths.forEach { deleteImage(it) }
    }

    /**
     * Returns a [Bitmap] decoded from an image file path, or null if it doesn't exist.
     * Use this for loading in non-Compose contexts. For Compose, prefer Coil with File path.
     */
    fun loadBitmap(imagePath: String?): Bitmap? {
        if (imagePath.isNullOrBlank()) return null
        return try {
            val file = File(imagePath)
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Returns total size in bytes used by saved notification images.
     */
    fun getTotalStorageBytes(context: Context): Long {
        return try {
            val dir = getImageDir(context)
            dir.listFiles()?.sumOf { it.length() } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    // --- private helpers ---

    private fun getImageDir(context: Context): File {
        val dir = File(context.filesDir, IMAGE_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Scales down the bitmap proportionally if its width exceeds [MAX_IMAGE_WIDTH_PX].
     * Returns the original bitmap unchanged if no scaling is needed.
     */
    private fun scaleBitmapIfNeeded(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        if (width <= MAX_IMAGE_WIDTH_PX) return bitmap

        val scale = MAX_IMAGE_WIDTH_PX.toFloat() / width
        val newHeight = (bitmap.height * scale).toInt()
        return Bitmap.createScaledBitmap(bitmap, MAX_IMAGE_WIDTH_PX, newHeight, true)
    }
}
