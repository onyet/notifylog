package id.onyet.app.notifylog.data.repository

import id.onyet.app.notifylog.data.local.AppInfo
import id.onyet.app.notifylog.data.local.NotificationDao
import id.onyet.app.notifylog.data.local.NotificationLog
import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val notificationDao: NotificationDao) {
    
    val allNotifications: Flow<List<NotificationLog>> = notificationDao.getAll()
    
    val distinctApps: Flow<List<AppInfo>> = notificationDao.getDistinctApps()
    
    val notificationCount: Flow<Int> = notificationDao.getCount()
    
    suspend fun insert(log: NotificationLog): Long {
        return notificationDao.insert(log)
    }
    
    suspend fun getById(id: Long): NotificationLog? {
        return notificationDao.getById(id)
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
        notificationDao.deleteById(id)
    }
    
    suspend fun deleteAll() {
        notificationDao.deleteAll()
    }
    
    suspend fun deleteOlderThan(days: Int) {
        val timestamp = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        notificationDao.deleteOlderThan(timestamp)
    }
}
