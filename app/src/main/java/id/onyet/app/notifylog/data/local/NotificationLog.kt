package id.onyet.app.notifylog.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notification_logs",
    indices = [
        Index(value = ["package_name"]),
        Index(value = ["received_time"]),
        Index(value = ["posted_time"])
    ]
)
data class NotificationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "package_name")
    val packageName: String,
    
    @ColumnInfo(name = "app_name")
    val appName: String?,
    
    @ColumnInfo(name = "title")
    val title: String?,
    
    @ColumnInfo(name = "content")
    val content: String?,
    
    @ColumnInfo(name = "posted_time")
    val postedTime: Long,
    
    @ColumnInfo(name = "received_time")
    val receivedTime: Long,
    
    @ColumnInfo(name = "is_cleared")
    val isCleared: Int = 0
)
