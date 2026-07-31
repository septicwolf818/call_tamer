package pl.septicwolf818.calltamer.ui.addblock

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.provider.CallLog
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.septicwolf818.calltamer.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CallLogEntry(
    val number: String,
    val name: String?,
    val date: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBlockScreen(
    onNumberSelected: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AddBlockViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    var manualNumber by remember { mutableStateOf("") }
    var showCallLog by remember { mutableStateOf(false) }
    var callLogEntries by remember { mutableStateOf<List<CallLogEntry>>(emptyList()) }
    val scope = rememberCoroutineScope()

    val hasContactsPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.READ_CONTACTS
    ) == PackageManager.PERMISSION_GRANTED

    var pendingContactsLaunch by remember { mutableStateOf(false) }

    val contactsPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let {
            try {
                val contactId = it.lastPathSegment
                if (contactId != null) {
                    val cursor = context.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(contactId),
                        null
                    )
                    cursor?.use { c ->
                        if (c.moveToFirst()) {
                            val number = c.getString(0)?.replace("[^0-9+]".toRegex(), "")
                            if (number != null) onNumberSelected(number)
                        }
                    }
                }
            } catch (_: SecurityException) { }
        }
    }

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && pendingContactsLaunch) {
            pendingContactsLaunch = false
            contactsPickerLauncher.launch(null)
        }
    }

    val readCallLogLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showCallLog = true
            scope.launch {
                callLogEntries = queryCallLog(context.contentResolver)
            }
        }
    }

    fun launchContactsPicker() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            contactsPickerLauncher.launch(null)
        } else {
            pendingContactsLaunch = true
            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_block_a_number)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.nav_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = manualNumber,
                onValueChange = {
                    manualNumber = it
                    if (hasContactsPermission) {
                        viewModel.onNumberChanged(it)
                    }
                },
                label = { Text(stringResource(R.string.add_phone_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (suggestions.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.add_suggestions_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                ) {
                    Column {
                        suggestions.forEachIndexed { index, suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        manualNumber = suggestion.number
                                        viewModel.clearSuggestions()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Contacts,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.width(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = suggestion.name ?: suggestion.number,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (suggestion.name != null) {
                                        Text(
                                            text = suggestion.number,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            if (index < suggestions.lastIndex) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (manualNumber.isNotBlank()) {
                            onNumberSelected(manualNumber.trim())
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = stringResource(R.string.add_continue),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(stringResource(R.string.add_or_pick), style = MaterialTheme.typography.labelLarge)

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { launchContactsPicker() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Contacts, contentDescription = null)
                    Spacer(Modifier.width(16.dp))
                    Text(stringResource(R.string.add_from_contacts), style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.READ_CALL_LOG
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            showCallLog = true
                            scope.launch {
                                callLogEntries = queryCallLog(context.contentResolver)
                            }
                        } else {
                            readCallLogLauncher.launch(Manifest.permission.READ_CALL_LOG)
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.History, contentDescription = null)
                    Spacer(Modifier.width(16.dp))
                    Text(stringResource(R.string.add_from_recent_calls), style = MaterialTheme.typography.titleMedium)
                }
            }

            if (showCallLog) {
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.add_recent_header), style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                LazyColumn {
                    items(callLogEntries) { entry ->
                        CallLogItem(entry = entry, onClick = {
                            if (entry.number.isNotBlank()) {
                                onNumberSelected(entry.number)
                            }
                        })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun CallLogItem(entry: CallLogEntry, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.name ?: entry.number,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = dateFormat.format(Date(entry.date)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private suspend fun queryCallLog(contentResolver: ContentResolver): List<CallLogEntry> = withContext(Dispatchers.IO) {
    val entries = mutableListOf<CallLogEntry>()
    try {
        val cursor = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.DATE
            ),
            null,
            null,
            "${CallLog.Calls.DATE} DESC"
        )
        cursor?.use { c ->
            val numberCol = c.getColumnIndex(CallLog.Calls.NUMBER)
            val nameCol = c.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val dateCol = c.getColumnIndex(CallLog.Calls.DATE)
            var count = 0
            while (c.moveToNext() && count < 50) {
                val number = c.getString(numberCol) ?: ""
                val name = c.getString(nameCol)
                val date = c.getLong(dateCol)
                if (number.isNotBlank()) {
                    entries.add(CallLogEntry(number = number, name = name, date = date))
                    count++
                }
            }
        }
    } catch (_: Exception) { }
    entries
}
