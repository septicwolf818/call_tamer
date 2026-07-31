package pl.septicwolf818.calltamer.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import pl.septicwolf818.calltamer.MainActivity
import pl.septicwolf818.calltamer.R
import pl.septicwolf818.calltamer.data.model.BlockBehavior
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_EXPIRY = "block_expiry"
        const val CHANNEL_BLOCKED_REJECT = "blocked_calls_rejected"
        const val CHANNEL_BLOCKED_SILENCED = "blocked_calls_silenced"
        const val DEEP_LINK_SCHEME = "calltamer"
        const val DEEP_LINK_HOST = "block"
    }

    private val defaultImportances = mapOf(
        CHANNEL_EXPIRY to NotificationManager.IMPORTANCE_DEFAULT,
        CHANNEL_BLOCKED_REJECT to NotificationManager.IMPORTANCE_HIGH,
        CHANNEL_BLOCKED_SILENCED to NotificationManager.IMPORTANCE_LOW
    )

    init {
        createChannels()
    }

    private fun createChannels() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureChannel(
            manager = manager,
            id = CHANNEL_EXPIRY,
            name = context.getString(R.string.notification_channel_expiry_name),
            description = context.getString(R.string.notification_channel_expiry_desc),
            importance = defaultImportances.getValue(CHANNEL_EXPIRY)
        )
        ensureChannel(
            manager = manager,
            id = CHANNEL_BLOCKED_REJECT,
            name = context.getString(R.string.notification_channel_blocked_reject_name),
            description = context.getString(R.string.notification_channel_blocked_reject_desc),
            importance = defaultImportances.getValue(CHANNEL_BLOCKED_REJECT)
        )
        ensureChannel(
            manager = manager,
            id = CHANNEL_BLOCKED_SILENCED,
            name = context.getString(R.string.notification_channel_blocked_silence_name),
            description = context.getString(R.string.notification_channel_blocked_silence_desc),
            importance = defaultImportances.getValue(CHANNEL_BLOCKED_SILENCED)
        )
    }

    fun isChannelEnabled(channelId: String): Boolean {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return manager.getNotificationChannel(channelId)?.importance
            ?.takeIf { it != NotificationManager.IMPORTANCE_NONE } != null
    }

    fun setChannelEnabled(channelId: String, enabled: Boolean) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = manager.getNotificationChannel(channelId) ?: return
        channel.importance = if (enabled) {
            defaultImportances[channelId] ?: NotificationManager.IMPORTANCE_DEFAULT
        } else {
            NotificationManager.IMPORTANCE_NONE
        }
        manager.createNotificationChannel(channel)
    }

    private fun ensureChannel(
        manager: NotificationManager,
        id: String,
        name: String,
        description: String,
        importance: Int
    ) {
        val existing = manager.getNotificationChannel(id)
        if (existing != null && existing.name != name) {
            manager.deleteNotificationChannel(id)
        }
        if (manager.getNotificationChannel(id) == null) {
            manager.createNotificationChannel(
                NotificationChannel(id, name, importance).apply {
                    this.description = description
                }
            )
        }
    }

    fun notifyCallBlocked(ruleId: Long, displayName: String, behavior: BlockBehavior) {
        if (!notificationsEnabled()) return

        val rejected = behavior == BlockBehavior.REJECT
        val channel = if (rejected) CHANNEL_BLOCKED_REJECT else CHANNEL_BLOCKED_SILENCED
        val title = if (rejected) {
            context.getString(R.string.notification_blocked_title)
        } else {
            context.getString(R.string.notification_silenced_title)
        }
        val text = if (rejected) {
            context.getString(R.string.notification_blocked_text, displayName)
        } else {
            context.getString(R.string.notification_silenced_text, displayName)
        }

        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_stat_blocked)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(
                if (rejected) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_LOW
            )
            .setContentIntent(openRulePendingIntent(ruleId))
            .addAction(
                0,
                context.getString(R.string.notification_unblock_action),
                unblockPendingIntent(ruleId)
            )
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(ruleId.toInt(), notification)
    }

    fun notifyBlockExpired(ruleId: Long, numberDisplay: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_EXPIRY)
            .setSmallIcon(R.drawable.ic_stat_blocked)
            .setContentTitle(context.getString(R.string.notification_block_expired_title))
            .setContentText(context.getString(R.string.notification_block_expired_text, numberDisplay))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openRulePendingIntent(ruleId))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(ruleId.toInt(), notification)
    }

    private fun notificationsEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    private fun unblockPendingIntent(ruleId: Long): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            data = Uri.parse("$DEEP_LINK_SCHEME://$DEEP_LINK_HOST/$ruleId?confirmUnblock=true")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context,
            ruleId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun openRulePendingIntent(ruleId: Long): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            data = Uri.parse("$DEEP_LINK_SCHEME://$DEEP_LINK_HOST/$ruleId?confirmUnblock=false")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context,
            ruleId.toInt() + 1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
