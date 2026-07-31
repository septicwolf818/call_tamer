package pl.septicwolf818.calltamer.data.repository

import kotlinx.coroutines.flow.Flow
import pl.septicwolf818.calltamer.data.local.entity.BlockHistoryEntryEntity
import pl.septicwolf818.calltamer.data.model.CallAction

interface HistoryRepository {
    fun observeAll(): Flow<List<BlockHistoryEntryEntity>>
    suspend fun recordEntry(
        phoneNumberNormalized: String,
        contactName: String?,
        timestamp: Long,
        ruleId: Long?,
        action: CallAction
    )
    suspend fun clearAll()
}
