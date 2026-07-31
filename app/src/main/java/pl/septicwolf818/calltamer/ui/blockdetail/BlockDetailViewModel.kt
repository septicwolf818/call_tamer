package pl.septicwolf818.calltamer.ui.blockdetail

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
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class BlockDetailViewModel @Inject constructor(
    private val blockRepository: BlockRepository,
    private val expiryScheduler: BlockExpiryScheduler
) : ViewModel() {

    private val _state = MutableStateFlow(BlockDetailState())
    val state: StateFlow<BlockDetailState> = _state.asStateFlow()

    fun loadRule(ruleId: Long) {
        viewModelScope.launch {
            blockRepository.observeRuleById(ruleId).collect { rule ->
                if (!_state.value.deleted) {
                    _state.value = BlockDetailState(rule = rule, isLoading = false)
                }
            }
        }
    }

    fun unblock() {
        viewModelScope.launch {
            val rule = _state.value.rule ?: return@launch
            expiryScheduler.cancelExpiry(rule.id)
            blockRepository.deactivateRule(rule.id)
            _state.value = _state.value.copy(deleted = true)
        }
    }

    fun setBehavior(behavior: BlockBehavior) {
        viewModelScope.launch {
            val rule = _state.value.rule ?: return@launch
            blockRepository.updateRule(rule.copy(behavior = behavior))
        }
    }

    fun extendByMinutes(minutes: Long) {
        viewModelScope.launch {
            val rule = _state.value.rule ?: return@launch
            val now = Instant.now()
            val base = rule.expiresAt?.let {
                Instant.ofEpochMilli(it).takeIf { expiresAt -> expiresAt.isAfter(now) }
            } ?: now
            val newExpiresAt = base.plus(Duration.ofMinutes(minutes)).toEpochMilli()
            val updated = rule.copy(
                expiresAt = newExpiresAt,
                blockType = BlockType.TEMPORARY
            )
            blockRepository.updateRule(updated)
            expiryScheduler.scheduleExpiry(rule.id, newExpiresAt)
        }
    }

    fun convertToPermanent() {
        viewModelScope.launch {
            val rule = _state.value.rule ?: return@launch
            val updated = rule.copy(blockType = BlockType.PERMANENT, expiresAt = null)
            blockRepository.updateRule(updated)
            expiryScheduler.cancelExpiry(rule.id)
        }
    }
}
