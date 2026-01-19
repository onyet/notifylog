package id.onyet.app.notifylog.data.local

import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = NotificationLog::class)
@Entity(tableName = "notification_logs_fts")
data class NotificationLogFts(
    val title: String?,
    val content: String?
)