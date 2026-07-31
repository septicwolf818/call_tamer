package pl.septicwolf818.calltamer.data.repository

import kotlinx.coroutines.flow.Flow
import pl.septicwolf818.calltamer.data.local.dao.BlockRuleDao
import pl.septicwolf818.calltamer.data.local.entity.BlockRuleEntity
import pl.septicwolf818.calltamer.data.model.BlockBehavior
import pl.septicwolf818.calltamer.data.model.BlockType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockRepositoryImpl @Inject constructor(
    private val dao: BlockRuleDao
) : BlockRepository {

    override fun observeActiveRules(): Flow<List<BlockRuleEntity>> = dao.observeActiveRules()

    override fun observeAllRules(): Flow<List<BlockRuleEntity>> = dao.observeAllRules()

    override fun observeRuleById(id: Long): Flow<BlockRuleEntity?> = dao.observeById(id)

    override suspend fun getActiveRuleByNumber(normalizedNumber: String): BlockRuleEntity? =
        dao.findActiveRuleByNumber(normalizedNumber)

    override suspend fun getRuleById(id: Long): BlockRuleEntity? = dao.getById(id)

    override suspend fun insertRule(
        phoneNumberNormalized: String,
        rawNumberDisplay: String,
        contactName: String?,
        blockType: BlockType,
        behavior: BlockBehavior,
        createdAt: Long,
        expiresAt: Long?
    ): Long {
        val existing = dao.findActiveRuleByNumber(phoneNumberNormalized)
        if (existing != null) {
            dao.deactivate(existing.id)
        }
        val entity = BlockRuleEntity(
            phoneNumberNormalized = phoneNumberNormalized,
            rawNumberDisplay = rawNumberDisplay,
            contactName = contactName,
            blockType = blockType,
            behavior = behavior,
            createdAt = createdAt,
            expiresAt = expiresAt,
            isActive = true
        )
        return dao.insert(entity)
    }

    override suspend fun updateRule(rule: BlockRuleEntity) = dao.update(rule)

    override suspend fun deactivateRule(id: Long) = dao.deactivate(id)

    override suspend fun deleteRule(id: Long) = dao.deleteById(id)

    override suspend fun getExpiredTemporaryRules(now: Long): List<BlockRuleEntity> =
        dao.getExpiredTemporaryRules(now)

    override suspend fun deactivateExpiredRules(now: Long): Int =
        dao.deactivateExpired(now)

    override suspend fun getAllActiveRules(): List<BlockRuleEntity> = dao.getAllActiveRules()
}
