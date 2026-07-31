package pl.septicwolf818.calltamer.ui.home

import pl.septicwolf818.calltamer.data.local.entity.BlockRuleEntity

data class HomeUiState(
    val activeRules: List<BlockRuleEntity> = emptyList(),
    val currentTimeMillis: Long = System.currentTimeMillis(),
    val showRoleWarning: Boolean = false
)
