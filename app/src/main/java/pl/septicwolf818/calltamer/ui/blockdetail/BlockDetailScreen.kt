package pl.septicwolf818.calltamer.ui.blockdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.septicwolf818.calltamer.R
import pl.septicwolf818.calltamer.data.model.BlockBehavior
import pl.septicwolf818.calltamer.data.model.BlockType
import pl.septicwolf818.calltamer.ui.formatRemaining
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockDetailScreen(
    ruleId: Long,
    confirmUnblock: Boolean = false,
    onChangeDuration: (String, Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: BlockDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showUnblockConfirm by remember { mutableStateOf(confirmUnblock) }

    LaunchedEffect(ruleId) {
        viewModel.loadRule(ruleId)
    }

    LaunchedEffect(state.deleted) {
        if (state.deleted) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_block_details)) },
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
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }

        val rule = state.rule ?: return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = rule.contactName ?: rule.rawNumberDisplay,
                style = MaterialTheme.typography.headlineMedium
            )
            if (rule.contactName != null) {
                Text(
                    text = rule.rawNumberDisplay,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow(stringResource(R.string.detail_type),
                        if (rule.blockType == BlockType.TEMPORARY) stringResource(R.string.detail_temporary) else stringResource(R.string.detail_permanent))
                    DetailRow(stringResource(R.string.detail_behavior), rule.behavior.name)
                    DetailRow(stringResource(R.string.detail_created), SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(rule.createdAt)))
                    if (rule.expiresAt != null) {
                        DetailRow(stringResource(R.string.detail_expires), SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(rule.expiresAt)))
                        DetailRow(stringResource(R.string.detail_remaining), formatRemaining(rule.expiresAt, System.currentTimeMillis()))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(stringResource(R.string.duration_behavior_label), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = rule.behavior == BlockBehavior.REJECT,
                    onClick = { viewModel.setBehavior(BlockBehavior.REJECT) },
                    label = { Text(stringResource(R.string.duration_reject)) }
                )
                FilterChip(
                    selected = rule.behavior == BlockBehavior.SILENCE,
                    onClick = { viewModel.setBehavior(BlockBehavior.SILENCE) },
                    label = { Text(stringResource(R.string.duration_silence)) }
                )
            }

            Spacer(Modifier.height(24.dp))

            if (rule.blockType == BlockType.TEMPORARY) {
                Text(stringResource(R.string.detail_extend_by), style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = { viewModel.extendByMinutes(60) }, label = { Text(stringResource(R.string.detail_extend_1h)) })
                    AssistChip(onClick = { viewModel.extendByMinutes(120) }, label = { Text(stringResource(R.string.detail_extend_2h)) })
                    AssistChip(onClick = { viewModel.extendByMinutes(480) }, label = { Text(stringResource(R.string.detail_extend_8h)) })
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.convertToPermanent() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.detail_convert_permanent))
                }
            } else {
                Button(
                    onClick = { onChangeDuration(rule.rawNumberDisplay, rule.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.detail_convert_temporary))
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { showUnblockConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(stringResource(R.string.detail_unblock))
            }
        }
    }

    if (showUnblockConfirm && state.rule != null) {
        AlertDialog(
            onDismissRequest = { showUnblockConfirm = false },
            title = { Text(stringResource(R.string.unblock_confirm_title)) },
            text = { Text(stringResource(R.string.unblock_confirm_message, state.rule!!.rawNumberDisplay)) },
            confirmButton = {
                TextButton(onClick = {
                    showUnblockConfirm = false
                    viewModel.unblock()
                }) {
                    Text(stringResource(R.string.unblock_confirm_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnblockConfirm = false }) {
                    Text(stringResource(R.string.unblock_confirm_cancel))
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.6f)
        )
    }
}
