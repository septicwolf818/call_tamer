package pl.septicwolf818.calltamer.ui.blockdetail

import pl.septicwolf818.calltamer.data.local.entity.BlockRuleEntity

data class BlockDetailState(
    val rule: BlockRuleEntity? = null,
    val isLoading: Boolean = true,
    val deleted: Boolean = false
)
