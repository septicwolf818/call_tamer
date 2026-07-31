package pl.septicwolf818.calltamer.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import pl.septicwolf818.calltamer.data.model.CallAction

@Entity(
    tableName = "block_history",
    foreignKeys = [
        ForeignKey(
            entity = BlockRuleEntity::class,
            parentColumns = ["id"],
            childColumns = ["ruleId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("ruleId")]
)
data class BlockHistoryEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumberNormalized: String,
    val contactName: String? = null,
    val timestamp: Long,
    val ruleId: Long? = null,
    val action: CallAction
)
