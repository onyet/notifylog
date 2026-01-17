

# Android Notification Logger App
Arsitektur Aplikasi Android untuk Menyimpan Log Notifikasi  
(Pakai SQLite, Offline, Mudah Dikembangkan)

---

## 🎯 Tujuan Aplikasi

Aplikasi ini bertujuan untuk:
- Merekam **semua notifikasi masuk** di Android
- Menyimpan data **secara lokal (SQLite)**
- Menampilkan **riwayat notifikasi**
- Filter berdasarkan:
  - Aplikasi
  - Rentang tanggal
- Search berdasarkan:
  - Judul notifikasi
  - Isi notifikasi

Target platform: **Android only**

---

## Arsitektur Umum

```

┌──────────────────────────────┐
│ NotificationListenerService  │
│ (Android System API)         │
└───────────────┬──────────────┘
↓
┌──────────────────────────────┐
│ NotificationParser           │
│ (Normalize & Extract Data)   │
└───────────────┬──────────────┘
↓
┌──────────────────────────────┐
│ NotificationRepository       │
│ (Single Source of Truth)     │
└───────────────┬──────────────┘
↓
┌──────────────────────────────┐
│ SQLite Database              │
│ (Room / Raw SQLite)          │
└───────────────┬──────────────┘
↓
┌──────────────────────────────┐
│ UI Layer                     │
│ (Activity / Fragment)        │
└──────────────────────────────┘

````

---

## 📦 Data Model

### Table: `notification_logs`

```sql
CREATE TABLE notification_logs (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  package_name TEXT NOT NULL,
  app_name TEXT,
  title TEXT,
  content TEXT,
  posted_time INTEGER,
  received_time INTEGER,
  is_cleared INTEGER DEFAULT 0
);
````

### Penjelasan Kolom

* `package_name` → ID aplikasi pengirim notifikasi
* `app_name` → Nama aplikasi (user friendly)
* `title` → Judul notifikasi
* `content` → Isi notifikasi
* `posted_time` → Waktu asli notifikasi dibuat
* `received_time` → Waktu diterima oleh service
* `is_cleared` → Status notifikasi dihapus/dismiss

---

## 🔔 Notification Listener Service

### File: `NotificationLogService.kt`

```kotlin
class NotificationLogService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras

        val title = extras.getString(Notification.EXTRA_TITLE)
        val content = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()

        val log = NotificationLog(
            packageName = sbn.packageName,
            appName = getAppName(sbn.packageName),
            title = title,
            content = content,
            postedTime = sbn.postTime,
            receivedTime = System.currentTimeMillis(),
            isCleared = 0
        )

        NotificationRepository.insert(log)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        NotificationRepository.markAsCleared(
            sbn.packageName,
            sbn.postTime
        )
    }
}
```

> Service ini adalah pintu masuk semua notifikasi sistem.

---

## 🧠 Repository Pattern

### File: `NotificationRepository.kt`

```kotlin
object NotificationRepository {

    fun insert(log: NotificationLog) {
        AppDatabase.instance.notificationDao().insert(log)
    }

    fun getAll(): List<NotificationLog> {
        return AppDatabase.instance.notificationDao().getAll()
    }

    fun filterByApp(packageName: String): List<NotificationLog> {
        return AppDatabase.instance.notificationDao()
            .getByPackage(packageName)
    }

    fun search(query: String): List<NotificationLog> {
        return AppDatabase.instance.notificationDao()
            .searchByContent(query)
    }

    fun filterByDate(start: Long, end: Long): List<NotificationLog> {
        return AppDatabase.instance.notificationDao()
            .filterByDate(start, end)
    }

    fun markAsCleared(pkg: String, postedTime: Long) {
        AppDatabase.instance.notificationDao()
            .markCleared(pkg, postedTime)
    }
}
```

> Semua akses database **HARUS lewat Repository**
> (Ini memudahkan Copilot & refactor)

---

## 🗄️ DAO (Query SQLite)

### File: `NotificationDao.kt`

```kotlin
@Dao
interface NotificationDao {

    @Insert
    fun insert(log: NotificationLog)

    @Query("""
        SELECT * FROM notification_logs
        ORDER BY received_time DESC
    """)
    fun getAll(): List<NotificationLog>

    @Query("""
        SELECT * FROM notification_logs
        WHERE package_name = :pkg
    """)
    fun getByPackage(pkg: String): List<NotificationLog>

    @Query("""
        SELECT * FROM notification_logs
        WHERE title LIKE '%' || :query || '%'
           OR content LIKE '%' || :query || '%'
    """)
    fun searchByContent(query: String): List<NotificationLog>

    @Query("""
        SELECT * FROM notification_logs
        WHERE received_time BETWEEN :start AND :end
    """)
    fun filterByDate(start: Long, end: Long): List<NotificationLog>

    @Query("""
        UPDATE notification_logs
        SET is_cleared = 1
        WHERE package_name = :pkg
          AND posted_time = :postedTime
    """)
    fun markCleared(pkg: String, postedTime: Long)
}
```

---

## 📱 UI Flow

### 1. History Screen

* List semua notifikasi
* Sort terbaru
* Icon aplikasi
* Judul & preview isi

### 2. Filter

* Dropdown daftar aplikasi
* Date picker (start – end)

### 3. Search

* Search bar
* Real-time filtering

---

## 🔐 Permission Wajib

User harus mengaktifkan secara manual:

```
Settings → Notification Access → App Name
```

Permission utama:

```xml
<service
    android:name=".NotificationLogService"
    android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE">
    <intent-filter>
        <action android:name="android.service.notification.NotificationListenerService" />
    </intent-filter>
</service>
```

---

## ⚠️ Catatan Play Store

* Wajib Privacy Policy
* Jelaskan:

  * Data hanya disimpan lokal
  * Tidak dikirim ke server
* Hindari klaim:

  * spying
  * monitoring diam-diam

---

## 🚀 Tambahan Fitur

* Export CSV
* Auto delete (retention)
* Statistik notifikasi
* Tag / label
* Backup lokal

---