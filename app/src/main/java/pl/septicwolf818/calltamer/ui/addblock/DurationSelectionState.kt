package pl.septicwolf818.calltamer.ui.addblock

import pl.septicwolf818.calltamer.data.model.BlockBehavior

data class DurationSelectionState(
    val phoneNumber: String = "",
    val selectedBehavior: BlockBehavior = BlockBehavior.REJECT,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val existingRuleId: Long? = null
)
