package pl.septicwolf818.calltamer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.septicwolf818.calltamer.data.repository.BlockRepository
import pl.septicwolf818.calltamer.domain.BlockExpiryScheduler
import javax.inject.Inject

/**
 * Reschedules all active expiry alarms after device reboot,
 * since AlarmManager alarms are cleared on reboot.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var blockRepository: BlockRepository
    @Inject lateinit var expiryScheduler: BlockExpiryScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) return

        CoroutineScope(Dispatchers.IO).launch {
            val activeRules = blockRepository.getAllActiveRules()
            val pairs = activeRules
                .filter { it.expiresAt != null }
                .map { BlockExpiryScheduler.LongPair(it.id, it.expiresAt!!) }
            expiryScheduler.rescheduleAll(pairs)
        }
    }
}
