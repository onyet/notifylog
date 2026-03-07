package id.onyet.app.notifylog.data.repository

import android.content.Context
import id.onyet.app.notifylog.data.local.AppInfo
import id.onyet.app.notifylog.data.local.NotificationDao
import id.onyet.app.notifylog.data.local.NotificationLog
import id.onyet.app.notifylog.util.NotificationImageManager
import kotlinx.coroutines.flow.Flow

class NotificationRepository(
    private val notificationDao: NotificationDao,
    private val context: Context
) {
    
    val allNotifications: Flow<List<NotificationLog>> = notificationDao.getAll()
    
    val distinctApps: Flow<List<AppInfo>> = notificationDao.getDistinctApps()
    
    val notificationCount: Flow<Int> = notificationDao.getCount()
    
    suspend fun insert(log: NotificationLog): Long {
        return notificationDao.insert(log)
    }
    
    suspend fun getById(id: Long): NotificationLog? {
        return notificationDao.getById(id)
    }

    suspend fun getPage(limit: Int, offset: Int): List<NotificationLog> {
        return notificationDao.getPage(limit, offset)
    }

    suspend fun getFilteredPage(
        query: String,
        packageName: String?,
        startDate: Long?,
        endDate: Long?,
        limit: Int,
        offset: Int
    ): List<NotificationLog> {
        // If query is wildcard or empty, use LIKE-based query
        val trimmed = query.trim()
        if (trimmed.isBlank() || trimmed == "%") {
            return notificationDao.searchWithFiltersPage(query, packageName, startDate, endDate, limit, offset)
        }

        // Build FTS match query: split terms and append wildcard * for prefix matching
        val tokens = trimmed.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .map { it.replace("'", "''") + "*" }

        val matchQuery = tokens.joinToString(" OR ")
        return notificationDao.searchWithFtsPage(matchQuery, packageName, startDate, endDate, limit, offset)
    }
    
    fun getByPackage(packageName: String): Flow<List<NotificationLog>> {
        return notificationDao.getByPackage(packageName)
    }
    
    fun search(query: String): Flow<List<NotificationLog>> {
        return notificationDao.searchByContent(query)
    }
    
    fun filterByDate(start: Long, end: Long): Flow<List<NotificationLog>> {
        return notificationDao.filterByDate(start, end)
    }
    
    fun filterByPackageAndDate(packageName: String, start: Long, end: Long): Flow<List<NotificationLog>> {
        return notificationDao.filterByPackageAndDate(packageName, start, end)
    }
    
    fun searchWithFilters(
        query: String,
        packageName: String?,
        startDate: Long?,
        endDate: Long?
    ): Flow<List<NotificationLog>> {
        return notificationDao.searchWithFilters(query, packageName, startDate, endDate)
    }
    
    suspend fun markAsCleared(packageName: String, postedTime: Long) {
        notificationDao.markCleared(packageName, postedTime)
    }
    
    suspend fun delete(id: Long) {
        // Clean up image file before removing the record
        val imagePath = notificationDao.getImagePathById(id)
        NotificationImageManager.deleteImage(imagePath)
        notificationDao.deleteById(id)
    }
    
    suspend fun deleteAll() {
        // Remove all saved image files, then clear the table
        NotificationImageManager.deleteAllImages(context)
        notificationDao.deleteAll()
    }
    
    suspend fun deleteOlderThan(days: Int) {
        val timestamp = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        // Collect image paths of records that will be deleted, then clean up files
        val imagePaths = notificationDao.getImagePathsOlderThan(timestamp)
        NotificationImageManager.deleteImages(imagePaths)
        notificationDao.deleteOlderThan(timestamp)
    }

    /**
     * Deletes all saved image files and nullifies image_path in every log record.
     * Notification text logs are preserved — only the images are removed.
     * Use this for "free up storage" without clearing history.
     */
    suspend fun clearAllImages() {
        NotificationImageManager.deleteAllImages(context)
        notificationDao.clearAllImagePaths()
    }

    /** Returns total bytes used by saved notification images on disk. */
    fun getImageStorageBytes(): Long = NotificationImageManager.getTotalStorageBytes(context)
}
