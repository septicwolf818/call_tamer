package pl.septicwolf818.calltamer.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.septicwolf818.calltamer.data.local.entity.BlockHistoryEntryEntity
import pl.septicwolf818.calltamer.data.model.CallAction
import pl.septicwolf818.calltamer.data.repository.BlockRepository
import pl.septicwolf818.calltamer.data.repository.HistoryRepository
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class HistoryStats(
    val today: Int = 0,
    val week: Int = 0,
    val total: Int = 0
)

data class HistoryUiState(
    val entries: List<HistoryEntryUiModel> = emptyList(),
    val stats: HistoryStats = HistoryStats()
)

data class HistoryEntryUiModel(
    val entry: BlockHistoryEntryEntity,
    val isCurrentlyBlocked: Boolean
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val blockRepository: BlockRepository
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = combine(
        historyRepository.observeAll(),
        blockRepository.observeActiveRules()
    ) { entries, activeRules ->
        val blockedNumbers = activeRules.map { it.phoneNumberNormalized }.toSet()
        val models = entries.map { entry ->
            HistoryEntryUiModel(
                entry = entry,
                isCurrentlyBlocked = blockedNumbers.contains(entry.phoneNumberNormalized)
            )
        }
        HistoryUiState(
            entries = models,
            stats = computeStats(entries)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    private fun computeStats(list: List<BlockHistoryEntryEntity>): HistoryStats {
        val now = System.currentTimeMillis()
        val startOfToday = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val weekAgo = now - WEEK_MILLIS
        val blocked = list.filter { it.action != CallAction.ALLOWED }
        return HistoryStats(
            today = blocked.count { it.timestamp >= startOfToday },
            week = blocked.count { it.timestamp >= weekAgo },
            total = blocked.size
        )
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearAll()
        }
    }

    private companion object {
        const val WEEK_MILLIS = 7L * 24 * 60 * 60 * 1000
    }
}
