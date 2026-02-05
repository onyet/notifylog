package id.onyet.app.notifylog.util

import android.content.Context
import id.onyet.app.notifylog.data.local.NotificationLog
import java.io.File
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {
    fun writeNotificationsToCsv(context: Context, notifications: List<NotificationLog>): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(context.cacheDir, "notifylog_export_$timestamp.csv")

        OutputStreamWriter(file.outputStream()).use { writer ->
            // Write header
            writer.append("id,package_name,app_name,title,content,posted_time,received_time,is_cleared\n")

            // Write rows with basic CSV escaping (double quotes)
            notifications.forEach { n ->
                fun esc(s: String?): String {
                    if (s == null) return ""
                    val replaced = s.replace("\"", "\"\"")
                    return "\"$replaced\""
                }

                val line = listOf(
                    n.id.toString(),
                    esc(n.packageName),
                    esc(n.appName),
                    esc(n.title),
                    esc(n.content),
                    n.postedTime.toString(),
                    n.receivedTime.toString(),
                    n.isCleared.toString()
                ).joinToString(",")

                writer.append(line)
                writer.append("\n")
            }

            writer.flush()
        }

        return file
    }
}
