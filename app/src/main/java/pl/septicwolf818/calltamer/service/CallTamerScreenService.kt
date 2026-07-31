package pl.septicwolf818.calltamer.service

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.septicwolf818.calltamer.data.model.BlockBehavior
import pl.septicwolf818.calltamer.data.model.CallAction
import pl.septicwolf818.calltamer.data.repository.BlockRepository
import pl.septicwolf818.calltamer.data.repository.HistoryRepository
import pl.septicwolf818.calltamer.domain.NumberNormalizer
import javax.inject.Inject

@AndroidEntryPoint
class CallTamerScreenService : CallScreeningService() {

    @Inject lateinit var blockRepository: BlockRepository
    @Inject lateinit var historyRepository: HistoryRepository
    @Inject lateinit var numberNormalizer: NumberNormalizer
    @Inject lateinit var notificationHelper: NotificationHelper

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onScreenCall(details: Call.Details) {
        val incomingNumber = details.handle?.schemeSpecificPart ?: return
        val normalized = numberNormalizer.normalize(incomingNumber)
        val now = System.currentTimeMillis()

        scope.launch {
            val activeRule = blockRepository.getActiveRuleByNumber(normalized)

            val response = if (activeRule != null) {
                when (activeRule.behavior) {
                    BlockBehavior.REJECT -> CallResponse.Builder()
                        .setDisallowCall(true)
                        .setRejectCall(true)
                        .build()
                    BlockBehavior.SILENCE -> CallResponse.Builder()
                        .setDisallowCall(false)
                        .setSilenceCall(true)
                        .build()
                }
            } else {
                CallResponse.Builder().setDisallowCall(false).build()
            }

            respondToCall(details, response)

            if (activeRule != null) {
                val action = when (activeRule.behavior) {
                    BlockBehavior.REJECT -> CallAction.REJECTED
                    BlockBehavior.SILENCE -> CallAction.SILENCED
                }

                historyRepository.recordEntry(
                    phoneNumberNormalized = normalized,
                    contactName = activeRule.contactName,
                    timestamp = now,
                    ruleId = activeRule.id,
                    action = action
                )

                val displayName = activeRule.contactName ?: incomingNumber
                notificationHelper.notifyCallBlocked(activeRule.id, displayName, activeRule.behavior)

                Log.i("CallTamer", "Blocked call from $normalized (${activeRule.behavior})")
            } else {
                historyRepository.recordEntry(
                    phoneNumberNormalized = normalized,
                    contactName = null,
                    timestamp = now,
                    ruleId = null,
                    action = CallAction.ALLOWED
                )

                Log.i("CallTamer", "Allowed call from $normalized (no active rule)")
            }
        }
    }
}
