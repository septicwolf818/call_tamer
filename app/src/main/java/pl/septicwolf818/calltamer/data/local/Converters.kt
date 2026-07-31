package pl.septicwolf818.calltamer.data.local

import androidx.room.TypeConverter
import pl.septicwolf818.calltamer.data.model.BlockBehavior
import pl.septicwolf818.calltamer.data.model.BlockType
import pl.septicwolf818.calltamer.data.model.CallAction

class Converters {

    @TypeConverter
    fun fromBlockType(value: BlockType): String = value.name

    @TypeConverter
    fun toBlockType(value: String): BlockType = BlockType.valueOf(value)

    @TypeConverter
    fun fromBlockBehavior(value: BlockBehavior): String = value.name

    @TypeConverter
    fun toBlockBehavior(value: String): BlockBehavior = BlockBehavior.valueOf(value)

    @TypeConverter
    fun fromCallAction(value: CallAction): String = value.name

    @TypeConverter
    fun toCallAction(value: String): CallAction = CallAction.valueOf(value)
}
