package pl.septicwolf818.calltamer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pl.septicwolf818.calltamer.data.local.entity.BlockRuleEntity

@Dao
interface BlockRuleDao {

    @Query("SELECT * FROM block_rules WHERE isActive = 1 ORDER BY createdAt DESC")
    fun observeActiveRules(): Flow<List<BlockRuleEntity>>

    @Query("SELECT * FROM block_rules WHERE phoneNumberNormalized = :number AND isActive = 1 LIMIT 1")
    suspend fun findActiveRuleByNumber(number: String): BlockRuleEntity?

    @Query("SELECT * FROM block_rules WHERE id = :id")
    suspend fun getById(id: Long): BlockRuleEntity?

    @Query("SELECT * FROM block_rules WHERE id = :id")
    fun observeById(id: Long): Flow<BlockRuleEntity?>

    @Query("SELECT * FROM block_rules WHERE isActive = 1")
    suspend fun getAllActiveRules(): List<BlockRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: BlockRuleEntity): Long

    @Update
    suspend fun update(rule: BlockRuleEntity)

    @Query("UPDATE block_rules SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)

    @Query("UPDATE block_rules SET isActive = 0 WHERE expiresAt IS NOT NULL AND expiresAt <= :now")
    suspend fun deactivateExpired(now: Long): Int
}
