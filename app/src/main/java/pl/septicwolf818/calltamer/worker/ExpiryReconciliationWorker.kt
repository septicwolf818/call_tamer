package pl.septicwolf818.calltamer.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import pl.septicwolf818.calltamer.data.repository.BlockRepository
import java.util.concurrent.TimeUnit

/**
 * Periodic reconciliation job that checks for expired-but-still-active rules
 * and cleans them up. Runs every 15 minutes as a safety net for any alarms
 * that were missed due to doze mode, reboot timing, or other edge cases.
 *
 * This is NOT the primary expiry mechanism — AlarmManager provides exact
 * expiry. This worker catches any drifted/missed cases.
 */
@HiltWorker
class ExpiryReconciliationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val blockRepository: BlockRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()
        val deactivated = blockRepository.deactivateExpiredRules(now)
        return Result.success()
    }

    companion object {
        private const val UNIQUE_NAME = "expiry_reconciliation"

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<ExpiryReconciliationWorker>(
                15, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
