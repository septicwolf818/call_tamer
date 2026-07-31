package pl.septicwolf818.calltamer.ui.addblock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.septicwolf818.calltamer.domain.ContactSuggestion
import pl.septicwolf818.calltamer.domain.ContactSuggestionProvider
import javax.inject.Inject

@HiltViewModel
class AddBlockViewModel @Inject constructor(
    private val contactSuggestionProvider: ContactSuggestionProvider
) : ViewModel() {

    private val _suggestions = MutableStateFlow<List<ContactSuggestion>>(emptyList())
    val suggestions: StateFlow<List<ContactSuggestion>> = _suggestions.asStateFlow()

    private var searchJob: Job? = null

    fun onNumberChanged(query: String) {
        searchJob?.cancel()
        if (query.trim().length < MIN_QUERY_LENGTH) {
            _suggestions.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_MILLIS)
            _suggestions.value = contactSuggestionProvider.search(query.trim())
        }
    }

    fun clearSuggestions() {
        searchJob?.cancel()
        _suggestions.value = emptyList()
    }

    private companion object {
        const val MIN_QUERY_LENGTH = 3
        const val DEBOUNCE_MILLIS = 300L
    }
}
