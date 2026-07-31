package pl.septicwolf818.calltamer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.septicwolf818.calltamer.data.model.BlockBehavior
import pl.septicwolf818.calltamer.data.model.BlockType

@Entity(tableName = "block_rules")
data class BlockRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumberNormalized: String,
    val rawNumberDisplay: String,
    val contactName: String? = null,
    val blockType: BlockType,
    val behavior: BlockBehavior,
    val createdAt: Long,
    val expiresAt: Long? = null,
    val isActive: Boolean = true
)
