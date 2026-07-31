package pl.septicwolf818.calltamer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pl.septicwolf818.calltamer.data.local.dao.BlockHistoryDao
import pl.septicwolf818.calltamer.data.local.dao.BlockRuleDao
import pl.septicwolf818.calltamer.data.local.entity.BlockHistoryEntryEntity
import pl.septicwolf818.calltamer.data.local.entity.BlockRuleEntity

@Database(
    entities = [BlockRuleEntity::class, BlockHistoryEntryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CallTamerDatabase : RoomDatabase() {
    abstract fun blockRuleDao(): BlockRuleDao
    abstract fun blockHistoryDao(): BlockHistoryDao
}
