package pl.septicwolf818.calltamer.ui.onboarding

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

enum class PermissionStepType {
    SCREENING_ROLE,
    NOTIFICATIONS,
    CONTACTS,
    CALL_LOG
}

data class OnboardingState(
    val steps: List<PermissionStepType> = emptyList(),
    val currentIndex: Int = 0,
    val started: Boolean = false,
    val isComplete: Boolean = false
) {
    val currentStep: PermissionStepType? get() = steps.getOrNull(currentIndex)
    val totalSteps: Int get() = steps.size
}

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun initSteps(steps: List<PermissionStepType>) {
        if (steps.isEmpty()) {
            _state.value = OnboardingState(isComplete = true)
        } else {
            _state.value = OnboardingState(steps = steps, currentIndex = 0)
        }
    }

    fun nextStep() {
        val current = _state.value
        if (!current.started) {
            _state.value = current.copy(started = true)
            return
        }
        val nextIndex = current.currentIndex + 1
        if (nextIndex >= current.steps.size) {
            _state.value = current.copy(isComplete = true)
        } else {
            _state.value = current.copy(currentIndex = nextIndex)
        }
    }
}
