package pl.septicwolf818.calltamer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.septicwolf818.calltamer.data.local.entity.BlockHistoryEntryEntity

@Dao
interface BlockHistoryDao {

    @Query("SELECT * FROM block_history ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<BlockHistoryEntryEntity>>

    @Insert
    suspend fun insert(entry: BlockHistoryEntryEntity): Long

    @Query("DELETE FROM block_history")
    suspend fun deleteAll()
}
