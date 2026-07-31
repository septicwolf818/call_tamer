package pl.septicwolf818.calltamer.ui.history

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.septicwolf818.calltamer.R
import pl.septicwolf818.calltamer.data.local.entity.BlockHistoryEntryEntity
import pl.septicwolf818.calltamer.data.model.CallAction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBlockNumber: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var showClearConfirm by remember { mutableStateOf(false) }

    val filteredEntries = remember(entries, query) {
        if (query.isBlank()) {
            entries
        } else {
            entries.filter { entry ->
                entry.contactName?.contains(query, ignoreCase = true) == true ||
                    entry.phoneNumberNormalized.contains(query, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_call_history)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.nav_back))
                    }
                },
                actions = {
                    if (entries.isNotEmpty()) {
                        IconButton(onClick = { showClearConfirm = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = stringResource(R.string.history_clear_cd))
                        }
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
        ) {
            if (entries.isNotEmpty()) {
                StatsCard(stats = stats)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    label = { Text(stringResource(R.string.history_search_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
            }

            if (filteredEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(if (entries.isEmpty()) R.string.history_empty else R.string.history_no_matches),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredEntries, key = { it.id }) { entry ->
                        HistoryCard(
                            entry = entry,
                            onBlock = { onBlockNumber(entry.phoneNumberNormalized) },
                            onCall = {
                                val intent = Intent(
                                    Intent.ACTION_DIAL,
                                    Uri.parse("tel:${Uri.encode(entry.phoneNumberNormalized)}")
                                )
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(stringResource(R.string.history_clear_title)) },
            text = { Text(stringResource(R.string.history_clear_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearConfirm = false
                }) {
                    Text(stringResource(R.string.history_clear_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(stringResource(R.string.history_clear_cancel))
                }
            }
        )
    }
}

@Composable
private fun StatsCard(stats: HistoryStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Row(modifier = Modifier.padding(vertical = 12.dp)) {
            StatItem(label = stringResource(R.string.history_stats_today), value = stats.today, modifier = Modifier.weight(1f))
            StatItem(label = stringResource(R.string.history_stats_week), value = stats.week, modifier = Modifier.weight(1f))
            StatItem(label = stringResource(R.string.history_stats_total), value = stats.total, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatItem(label: String, value: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HistoryCard(
    entry: BlockHistoryEntryEntity,
    onBlock: () -> Unit,
    onCall: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val actionLabel = when (entry.action) {
        CallAction.REJECTED -> stringResource(R.string.history_rejected)
        CallAction.SILENCED -> stringResource(R.string.history_silenced)
        CallAction.ALLOWED -> stringResource(R.string.history_allowed)
    }
    val actionColor = when (entry.action) {
        CallAction.REJECTED -> MaterialTheme.colorScheme.error
        CallAction.SILENCED -> MaterialTheme.colorScheme.tertiary
        CallAction.ALLOWED -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = entry.contactName ?: entry.phoneNumberNormalized,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = actionColor
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = dateFormat.format(Date(entry.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (entry.action != CallAction.ALLOWED) {
                Spacer(Modifier.height(4.dp))
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                Row {
                    TextButton(onClick = onBlock) {
                        Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.history_block))
                    }
                    Spacer(Modifier.width(4.dp))
                    TextButton(onClick = onCall) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.history_call))
                    }
                }
            }
        }
    }
}
