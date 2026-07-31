package pl.septicwolf818.calltamer.data.repository

import kotlinx.coroutines.flow.Flow
import pl.septicwolf818.calltamer.data.local.entity.BlockRuleEntity
import pl.septicwolf818.calltamer.data.model.BlockBehavior
import pl.septicwolf818.calltamer.data.model.BlockType

interface BlockRepository {
    fun observeActiveRules(): Flow<List<BlockRuleEntity>>
    fun observeRuleById(id: Long): Flow<BlockRuleEntity?>
    suspend fun getActiveRuleByNumber(normalizedNumber: String): BlockRuleEntity?
    suspend fun getRuleById(id: Long): BlockRuleEntity?
    suspend fun insertRule(
        phoneNumberNormalized: String,
        rawNumberDisplay: String,
        contactName: String?,
        blockType: BlockType,
        behavior: BlockBehavior,
        createdAt: Long,
        expiresAt: Long?
    ): Long
    suspend fun updateRule(rule: BlockRuleEntity)
    suspend fun deactivateRule(id: Long)
    suspend fun deactivateExpiredRules(now: Long): Int
    suspend fun getAllActiveRules(): List<BlockRuleEntity>
}
