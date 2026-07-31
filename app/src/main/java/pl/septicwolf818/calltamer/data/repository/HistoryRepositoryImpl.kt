package pl.septicwolf818.calltamer.data.repository

import kotlinx.coroutines.flow.Flow
import pl.septicwolf818.calltamer.data.local.dao.BlockHistoryDao
import pl.septicwolf818.calltamer.data.local.entity.BlockHistoryEntryEntity
import pl.septicwolf818.calltamer.data.model.CallAction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val dao: BlockHistoryDao
) : HistoryRepository {

    override fun observeAll(): Flow<List<BlockHistoryEntryEntity>> = dao.observeAll()

    override suspend fun recordEntry(
        phoneNumberNormalized: String,
        contactName: String?,
        timestamp: Long,
        ruleId: Long?,
        action: CallAction
    ) {
        dao.insert(
            BlockHistoryEntryEntity(
                phoneNumberNormalized = phoneNumberNormalized,
                contactName = contactName,
                timestamp = timestamp,
                ruleId = ruleId,
                action = action
            )
        )
    }

    override suspend fun clearAll() = dao.deleteAll()
}
