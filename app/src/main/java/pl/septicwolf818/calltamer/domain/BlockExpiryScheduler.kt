package pl.septicwolf818.calltamer.domain

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import pl.septicwolf818.calltamer.service.BlockExpiryReceiver
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules exact alarms for temporary block expiry using AlarmManager.
 *
 * Why AlarmManager over WorkManager for expiry:
 * AlarmManager.setExactAndAllowWhileIdle fires near-exactly at the scheduled time
 * even when the device is in doze mode (with a ~1 second allowance window).
 * WorkManager's minimum interval is 15 minutes, which is too coarse for precise
 * expiry. However, WorkManager is still used as a periodic safety-net reconciliation
 * job to catch any missed alarms (doze edge cases, reboot without BootReceiver, etc.).
 *
 * On targetSdk 31+, USE_EXACT_ALARM permission is required for precise alarms.
 * On targetSdk 33+ (our minSdk), we request USE_EXACT_ALARM in the manifest.
 * Alarms are cleared on device reboot, so BootReceiver reschedules them.
 */
@Singleton
class BlockExpiryScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleExpiry(ruleId: Long, expiresAtMillis: Long) {
        val intent = Intent(context, BlockExpiryReceiver::class.java).apply {
            putExtra(EXTRA_RULE_ID, ruleId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ruleId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            expiresAtMillis,
            pendingIntent
        )
    }

    fun cancelExpiry(ruleId: Long) {
        val intent = Intent(context, BlockExpiryReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ruleId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun rescheduleAll(rules: List<LongPair>) {
        for ((ruleId, expiresAt) in rules) {
            scheduleExpiry(ruleId, expiresAt)
        }
    }

    data class LongPair(val ruleId: Long, val expiresAt: Long)

    companion object {
        const val EXTRA_RULE_ID = "rule_id"
    }
}
