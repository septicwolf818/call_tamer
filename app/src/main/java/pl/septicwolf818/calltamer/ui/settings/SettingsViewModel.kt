package pl.septicwolf818.calltamer.ui.settings

import android.app.role.RoleManager
import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.LocaleList
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import pl.septicwolf818.calltamer.data.preferences.AppLanguage
import pl.septicwolf818.calltamer.data.preferences.SettingsRepository
import pl.septicwolf818.calltamer.data.preferences.ThemeMode
import pl.septicwolf818.calltamer.data.repository.BlockRepository
import pl.septicwolf818.calltamer.service.NotificationHelper
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val blockRepository: BlockRepository,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _isScreenRoleHeld = MutableStateFlow(false)
    val isScreenRoleHeld: StateFlow<Boolean> = _isScreenRoleHeld.asStateFlow()

    private val _notifyRejectedCalls = MutableStateFlow(false)
    val notifyRejectedCalls: StateFlow<Boolean> = _notifyRejectedCalls.asStateFlow()

    private val _notifySilencedCalls = MutableStateFlow(false)
    val notifySilencedCalls: StateFlow<Boolean> = _notifySilencedCalls.asStateFlow()

    private val _notifyBlockExpiry = MutableStateFlow(false)
    val notifyBlockExpiry: StateFlow<Boolean> = _notifyBlockExpiry.asStateFlow()

    private val _isIgnoringBatteryOptimizations = MutableStateFlow(false)
    val isIgnoringBatteryOptimizations: StateFlow<Boolean> =
        _isIgnoringBatteryOptimizations.asStateFlow()

    val activeBlockCount: StateFlow<Int> = blockRepository.observeActiveRules()
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode

    val currentLanguage: AppLanguage
        get() = settingsRepository.currentLanguage

    init {
        checkRoleStatus()
        refreshNotificationChannels()
        refreshBatteryOptimizationStatus()
    }

    fun refreshNotificationChannels() {
        _notifyRejectedCalls.value =
            notificationHelper.isChannelEnabled(NotificationHelper.CHANNEL_BLOCKED_REJECT)
        _notifySilencedCalls.value =
            notificationHelper.isChannelEnabled(NotificationHelper.CHANNEL_BLOCKED_SILENCED)
        _notifyBlockExpiry.value =
            notificationHelper.isChannelEnabled(NotificationHelper.CHANNEL_EXPIRY)
    }

    fun checkRoleStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
            _isScreenRoleHeld.value = roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        settingsRepository.setThemeMode(mode)
    }

    fun setNotifyRejectedCalls(enabled: Boolean) {
        notificationHelper.setChannelEnabled(NotificationHelper.CHANNEL_BLOCKED_REJECT, enabled)
        _notifyRejectedCalls.value = enabled
    }

    fun setNotifySilencedCalls(enabled: Boolean) {
        notificationHelper.setChannelEnabled(NotificationHelper.CHANNEL_BLOCKED_SILENCED, enabled)
        _notifySilencedCalls.value = enabled
    }

    fun setNotifyBlockExpiry(enabled: Boolean) {
        notificationHelper.setChannelEnabled(NotificationHelper.CHANNEL_EXPIRY, enabled)
        _notifyBlockExpiry.value = enabled
    }

    fun setLanguage(language: AppLanguage) {
        settingsRepository.setLanguage(language)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            localeManager.applicationLocales = when (language) {
                AppLanguage.ENGLISH -> LocaleList.forLanguageTags("en")
                AppLanguage.POLISH -> LocaleList.forLanguageTags("pl")
                AppLanguage.SYSTEM -> LocaleList.getEmptyLocaleList()
            }
        }
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun requestIgnoreBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            if (powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } else {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = android.net.Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }
    }

    fun refreshBatteryOptimizationStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            _isIgnoringBatteryOptimizations.value =
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
        }
    }
}
