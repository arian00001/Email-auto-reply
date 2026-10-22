package site.arian00001.devstyleautoreply

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReplyWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val store = SecureStore(applicationContext)
            if (!store.enabled || !store.hasCredentials()) return Result.success()
            GmailService(applicationContext).processUnread()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
