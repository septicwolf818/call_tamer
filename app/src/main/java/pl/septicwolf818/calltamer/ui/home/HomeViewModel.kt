package pl.septicwolf818.calltamer.ui.home

import android.app.role.RoleManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.septicwolf818.calltamer.data.repository.BlockRepository
import pl.septicwolf818.calltamer.domain.ContactNameResolver
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val blockRepository: BlockRepository,
    private val contactNameResolver: ContactNameResolver,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        checkRoleStatus()

        viewModelScope.launch {
            blockRepository.observeActiveRules().collect { rules ->
                _uiState.value = _uiState.value.copy(activeRules = rules)
                rules.filter { it.contactName == null }.forEach { rule ->
                    resolveContactName(rule)
                }
            }
        }

        viewModelScope.launch {
            while (true) {
                delay(60_000L)
                _uiState.value = _uiState.value.copy(
                    currentTimeMillis = System.currentTimeMillis()
                )
            }
        }
    }

    private fun resolveContactName(rule: pl.septicwolf818.calltamer.data.local.entity.BlockRuleEntity) {
        viewModelScope.launch {
            val name = contactNameResolver.resolveName(rule.rawNumberDisplay)
            if (name != null) {
                blockRepository.updateRule(rule.copy(contactName = name))
            }
        }
    }

    fun checkRoleStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
            val isHeld = roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
            _uiState.value = _uiState.value.copy(showRoleWarning = !isHeld)
        }
    }
}
