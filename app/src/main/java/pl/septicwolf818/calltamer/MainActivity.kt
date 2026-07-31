package pl.septicwolf818.calltamer

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import pl.septicwolf818.calltamer.data.preferences.AppLanguage
import pl.septicwolf818.calltamer.data.preferences.SettingsRepository
import pl.septicwolf818.calltamer.data.preferences.ThemeMode
import pl.septicwolf818.calltamer.ui.CallTamerNavHost
import pl.septicwolf818.calltamer.ui.Screen
import pl.septicwolf818.calltamer.ui.theme.CallTamerTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        applySavedLanguage()
        setContent {
            val themeMode by settingsRepository.themeMode.collectAsState(ThemeMode.SYSTEM)
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            CallTamerTheme(darkTheme = darkTheme) {
                MainScreen()
            }
        }
    }

    private fun applySavedLanguage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = getSystemService(LocaleManager::class.java)
            val current = localeManager.applicationLocales.toLanguageTags()
            val target = when (settingsRepository.currentLanguage) {
                AppLanguage.ENGLISH -> "en"
                AppLanguage.POLISH -> "pl"
                AppLanguage.SYSTEM -> ""
            }
            if (current != target) {
                localeManager.applicationLocales = when (target) {
                    "en" -> LocaleList.forLanguageTags("en")
                    "pl" -> LocaleList.forLanguageTags("pl")
                    else -> LocaleList.getEmptyLocaleList()
                }
            }
        }
    }
}

@Composable
private fun MainScreen() {
    val navController = rememberNavController()
    var startDestination by remember { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("calltamer_prefs", Context.MODE_PRIVATE)
        val onboardingDone = prefs.getBoolean("onboarding_complete", false)
        startDestination = if (onboardingDone) Screen.Home.route else Screen.Onboarding.route
    }

    startDestination?.let { start ->
        CallTamerNavHost(
            navController = navController,
            onOnboardingComplete = {
                context.getSharedPreferences("calltamer_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("onboarding_complete", true)
                    .apply()
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            },
            startDestination = start
        )
    }
}
