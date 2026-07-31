package pl.septicwolf818.calltamer.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.septicwolf818.calltamer.R
import pl.septicwolf818.calltamer.data.preferences.AppLanguage
import pl.septicwolf818.calltamer.data.preferences.ThemeMode
import pl.septicwolf818.calltamer.ui.role.rememberCallScreeningRoleRequester

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isRoleHeld by viewModel.isScreenRoleHeld.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val notifyRejectedCalls by viewModel.notifyRejectedCalls.collectAsStateWithLifecycle()
    val notifySilencedCalls by viewModel.notifySilencedCalls.collectAsStateWithLifecycle()
    val notifyBlockExpiry by viewModel.notifyBlockExpiry.collectAsStateWithLifecycle()
    val activeBlockCount by viewModel.activeBlockCount.collectAsStateWithLifecycle()
    val isIgnoringBatteryOptimizations by viewModel.isIgnoringBatteryOptimizations.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val requestRole = rememberCallScreeningRoleRequester { viewModel.checkRoleStatus() }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkRoleStatus()
                viewModel.refreshNotificationChannels()
                viewModel.refreshBatteryOptimizationStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_settings)) },
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
        SettingsColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            RoleCard(
                isRoleHeld,
                context,
                requestRole,
                viewModel,
                isIgnoringBatteryOptimizations
            )
            Spacer(Modifier.height(16.dp))
            SummaryCard(activeBlockCount)
            Spacer(Modifier.height(16.dp))
            ThemeSection(themeMode, viewModel)
            Spacer(Modifier.height(16.dp))
            LanguageSection(viewModel)
            Spacer(Modifier.height(16.dp))
            NotifySection(
                notifyRejectedCalls = notifyRejectedCalls,
                notifySilencedCalls = notifySilencedCalls,
                notifyBlockExpiry = notifyBlockExpiry,
                viewModel = viewModel
            )
            Spacer(Modifier.height(16.dp))
            AboutCard()
        }
    }
}

@Composable
private fun SettingsColumn(
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        content = { content() }
    )
}

@Composable
private fun RoleCard(
    isRoleHeld: Boolean,
    context: android.content.Context,
    requestRole: () -> Unit,
    viewModel: SettingsViewModel,
    isIgnoringBatteryOptimizations: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.settings_role_title), style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(8.dp))

            val statusText = if (isRoleHeld) stringResource(R.string.settings_role_active)
            else stringResource(R.string.settings_role_not_granted)
            val statusColor = if (isRoleHeld) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error

            Text(
                text = stringResource(R.string.settings_role_status, statusText),
                style = MaterialTheme.typography.bodyMedium,
                color = statusColor
            )

            Spacer(Modifier.height(12.dp))

            if (isRoleHeld) {
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_change_app))
                }
            } else {
                Button(
                    onClick = { requestRole() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_role_grant))
                }
            }
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.openAppSettings() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_open_app))
            }
            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { viewModel.requestIgnoreBatteryOptimizations() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_background_protection))
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (isIgnoringBatteryOptimizations) {
                        stringResource(R.string.settings_background_protection_enabled)
                    } else {
                        stringResource(R.string.settings_background_protection_disabled)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isIgnoringBatteryOptimizations) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemeSection(themeMode: ThemeMode, viewModel: SettingsViewModel) {
    SettingsSection(title = stringResource(R.string.settings_theme_title)) {
        RadioSettingRow(
            label = stringResource(R.string.settings_theme_system),
            selected = themeMode == ThemeMode.SYSTEM,
            onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) }
        )
        RadioSettingRow(
            label = stringResource(R.string.settings_theme_light),
            selected = themeMode == ThemeMode.LIGHT,
            onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) }
        )
        RadioSettingRow(
            label = stringResource(R.string.settings_theme_dark),
            selected = themeMode == ThemeMode.DARK,
            onClick = { viewModel.setThemeMode(ThemeMode.DARK) }
        )
    }
}

@Composable
private fun LanguageSection(viewModel: SettingsViewModel) {
    SettingsSection(title = stringResource(R.string.settings_language_title)) {
        RadioSettingRow(
            label = stringResource(R.string.settings_language_system),
            selected = viewModel.currentLanguage == AppLanguage.SYSTEM,
            onClick = { viewModel.setLanguage(AppLanguage.SYSTEM) }
        )
        RadioSettingRow(
            label = stringResource(R.string.settings_language_english),
            selected = viewModel.currentLanguage == AppLanguage.ENGLISH,
            onClick = { viewModel.setLanguage(AppLanguage.ENGLISH) }
        )
        RadioSettingRow(
            label = stringResource(R.string.settings_language_polish),
            selected = viewModel.currentLanguage == AppLanguage.POLISH,
            onClick = { viewModel.setLanguage(AppLanguage.POLISH) }
        )
    }
}

@Composable
private fun NotifySection(
    notifyRejectedCalls: Boolean,
    notifySilencedCalls: Boolean,
    notifyBlockExpiry: Boolean,
    viewModel: SettingsViewModel
) {
    SettingsSection(title = stringResource(R.string.settings_notify_title)) {
        SwitchSettingRow(
            label = stringResource(R.string.settings_notify_rejected_desc),
            checked = notifyRejectedCalls,
            onCheckedChange = { viewModel.setNotifyRejectedCalls(it) }
        )
        SwitchSettingRow(
            label = stringResource(R.string.settings_notify_silenced_desc),
            checked = notifySilencedCalls,
            onCheckedChange = { viewModel.setNotifySilencedCalls(it) }
        )
        SwitchSettingRow(
            label = stringResource(R.string.settings_notify_expiry_desc),
            checked = notifyBlockExpiry,
            onCheckedChange = { viewModel.setNotifyBlockExpiry(it) }
        )
    }
}

@Composable
private fun SwitchSettingRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SummaryCard(activeBlockCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.settings_summary_title), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (activeBlockCount == 0) {
                    stringResource(R.string.settings_summary_empty)
                } else {
                    stringResource(R.string.settings_summary_active, activeBlockCount)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AboutCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.settings_about), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.settings_version),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.settings_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun RadioSettingRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
