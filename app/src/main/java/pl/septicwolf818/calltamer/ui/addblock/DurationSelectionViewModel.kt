package pl.septicwolf818.calltamer.ui.addblock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.septicwolf818.calltamer.data.model.BlockBehavior
import pl.septicwolf818.calltamer.data.model.BlockType
import pl.septicwolf818.calltamer.data.repository.BlockRepository
import pl.septicwolf818.calltamer.domain.BlockExpiryScheduler
import pl.septicwolf818.calltamer.domain.ContactNameResolver
import pl.septicwolf818.calltamer.domain.NumberNormalizer
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class DurationSelectionViewModel @Inject constructor(
    private val blockRepository: BlockRepository,
    private val numberNormalizer: NumberNormalizer,
    private val expiryScheduler: BlockExpiryScheduler,
    private val contactNameResolver: ContactNameResolver
) : ViewModel() {

    private val _state = MutableStateFlow(DurationSelectionState())
    val state: StateFlow<DurationSelectionState> = _state.asStateFlow()

    fun setPhoneNumber(number: String) {
        _state.value = _state.value.copy(phoneNumber = number)
    }

    fun setExistingRule(ruleId: Long) {
        viewModelScope.launch {
            val rule = blockRepository.getRuleById(ruleId) ?: return@launch
            _state.value = _state.value.copy(
                phoneNumber = rule.rawNumberDisplay,
                selectedBehavior = rule.behavior,
                existingRuleId = ruleId
            )
        }
    }

    fun setBehavior(behavior: BlockBehavior) {
        _state.value = _state.value.copy(selectedBehavior = behavior)
    }

    fun blockPermanent() {
        saveBlock(blockType = BlockType.PERMANENT, expiresAt = null)
    }

    fun blockTemporary(durationMinutes: Long) {
        val expiresAt = Instant.now().plus(Duration.ofMinutes(durationMinutes)).toEpochMilli()
        saveBlock(blockType = BlockType.TEMPORARY, expiresAt = expiresAt)
    }

    fun blockUntilTomorrow() {
        val zone = ZoneId.systemDefault()
        val tomorrow = ZonedDateTime.now(zone).toLocalDate().plusDays(1).atStartOfDay(zone).toInstant()
        saveBlock(blockType = BlockType.TEMPORARY, expiresAt = tomorrow.toEpochMilli())
    }

    fun blockCustom(expiresAtMillis: Long) {
        saveBlock(blockType = BlockType.TEMPORARY, expiresAt = expiresAtMillis)
    }

    private fun saveBlock(blockType: BlockType, expiresAt: Long?) {
        val current = _state.value
        if (current.isSaving) return
        _state.value = current.copy(isSaving = true)

        viewModelScope.launch {
            val normalized = numberNormalizer.normalize(current.phoneNumber)
            val now = System.currentTimeMillis()
            val existingId = current.existingRuleId

            if (existingId != null) {
                val rule = blockRepository.getRuleById(existingId)
                if (rule != null) {
                    blockRepository.updateRule(
                        rule.copy(
                            blockType = blockType,
                            behavior = current.selectedBehavior,
                            expiresAt = expiresAt
                        )
                    )
                    if (expiresAt != null) {
                        expiryScheduler.scheduleExpiry(existingId, expiresAt)
                    } else {
                        expiryScheduler.cancelExpiry(existingId)
                    }
                }
            } else {
                val contactName = contactNameResolver.resolveName(current.phoneNumber)
                val ruleId = blockRepository.insertRule(
                    phoneNumberNormalized = normalized,
                    rawNumberDisplay = current.phoneNumber,
                    contactName = contactName,
                    blockType = blockType,
                    behavior = current.selectedBehavior,
                    createdAt = now,
                    expiresAt = expiresAt
                )

                if (expiresAt != null) {
                    expiryScheduler.scheduleExpiry(ruleId, expiresAt)
                }
            }

            _state.value = _state.value.copy(isSaving = false, saved = true)
        }
    }
}
