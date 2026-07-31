package pl.septicwolf818.calltamer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.septicwolf818.calltamer.domain.BlockExpiryScheduler
import pl.septicwolf818.calltamer.data.repository.BlockRepository
import javax.inject.Inject

/**
 * Receives expiry alarm intents from AlarmManager.
 * Deactivates the associated block rule and shows a notification.
 */
@AndroidEntryPoint
class BlockExpiryReceiver : BroadcastReceiver() {

    @Inject lateinit var blockRepository: BlockRepository
    @Inject lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        val ruleId = intent.getLongExtra(BlockExpiryScheduler.EXTRA_RULE_ID, -1L)
        if (ruleId == -1L) return

        CoroutineScope(Dispatchers.IO).launch {
            val rule = blockRepository.getRuleById(ruleId)
            if (rule != null) {
                blockRepository.deactivateRule(ruleId)
                notificationHelper.notifyBlockExpired(ruleId, rule.rawNumberDisplay)
            }
        }
    }
}
