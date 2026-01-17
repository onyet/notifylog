package id.onyet.app.notifylog.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: NotificationLog): Long
    
    @Query("""
        SELECT * FROM notification_logs 
        ORDER BY received_time DESC
    """)
    fun getAll(): Flow<List<NotificationLog>>
    
    @Query("""
        SELECT * FROM notification_logs 
        ORDER BY received_time DESC
    """)
    suspend fun getAllSync(): List<NotificationLog>
    
    @Query("""
        SELECT * FROM notification_logs 
        WHERE id = :id
    """)
    suspend fun getById(id: Long): NotificationLog?
    
    @Query("""
        SELECT * FROM notification_logs 
        WHERE package_name = :packageName 
        ORDER BY received_time DESC
    """)
    fun getByPackage(packageName: String): Flow<List<NotificationLog>>
    
    @Query("""
        SELECT * FROM notification_logs 
        WHERE title LIKE '%' || :query || '%' 
           OR content LIKE '%' || :query || '%' 
        ORDER BY received_time DESC
    """)
    fun searchByContent(query: String): Flow<List<NotificationLog>>
    
    @Query("""
        SELECT * FROM notification_logs 
        WHERE received_time BETWEEN :start AND :end 
        ORDER BY received_time DESC
    """)
    fun filterByDate(start: Long, end: Long): Flow<List<NotificationLog>>
    
    @Query("""
        SELECT * FROM notification_logs 
        WHERE package_name = :packageName 
          AND received_time BETWEEN :start AND :end 
        ORDER BY received_time DESC
    """)
    fun filterByPackageAndDate(packageName: String, start: Long, end: Long): Flow<List<NotificationLog>>
    
    @Query("""
        SELECT * FROM notification_logs 
        WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
          AND (:packageName IS NULL OR package_name = :packageName)
          AND (:startDate IS NULL OR received_time >= :startDate)
          AND (:endDate IS NULL OR received_time <= :endDate)
        ORDER BY received_time DESC
    """)
    fun searchWithFilters(
        query: String,
        packageName: String?,
        startDate: Long?,
        endDate: Long?
    ): Flow<List<NotificationLog>>
    
    @Query("""
        UPDATE notification_logs 
        SET is_cleared = 1 
        WHERE package_name = :packageName 
          AND posted_time = :postedTime
    """)
    suspend fun markCleared(packageName: String, postedTime: Long)
    
    @Query("""
        SELECT DISTINCT package_name, app_name 
        FROM notification_logs 
        ORDER BY app_name
    """)
    fun getDistinctApps(): Flow<List<AppInfo>>
    
    @Query("DELETE FROM notification_logs WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM notification_logs")
    suspend fun deleteAll()
    
    @Query("DELETE FROM notification_logs WHERE received_time < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM notification_logs")
    fun getCount(): Flow<Int>
}

data class AppInfo(
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "app_name")
    val appName: String?
)
