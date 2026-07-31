package pl.septicwolf818.calltamer.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.septicwolf818.calltamer.data.local.entity.BlockHistoryEntryEntity
import pl.septicwolf818.calltamer.data.model.CallAction
import pl.septicwolf818.calltamer.data.repository.HistoryRepository
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class HistoryStats(
    val today: Int = 0,
    val week: Int = 0,
    val total: Int = 0
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _entries = MutableStateFlow<List<BlockHistoryEntryEntity>>(emptyList())
    val entries: StateFlow<List<BlockHistoryEntryEntity>> = _entries.asStateFlow()

    private val _stats = MutableStateFlow(HistoryStats())
    val stats: StateFlow<HistoryStats> = _stats.asStateFlow()

    init {
        viewModelScope.launch {
            historyRepository.observeAll().collect { list ->
                _entries.value = list
                _stats.value = computeStats(list)
            }
        }
    }

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
