package id.onyet.app.notifylog.update

import android.content.Context
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.ktx.requestAppUpdateInfo

import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import id.onyet.app.notifylog.update.UpdateNotificationHelper

class UpdateCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val appUpdateManager = AppUpdateManagerFactory.create(applicationContext)

            // Await appUpdateInfo without using Tasks.await (use coroutine-friendly suspendCancellableCoroutine)
            val appUpdateInfo = appUpdateManager.requestAppUpdateInfo()

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                UpdateNotificationHelper.showUpdateNotification(applicationContext)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
