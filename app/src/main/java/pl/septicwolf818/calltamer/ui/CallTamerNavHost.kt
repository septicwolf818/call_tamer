package pl.septicwolf818.calltamer.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import pl.septicwolf818.calltamer.ui.addblock.AddBlockScreen
import pl.septicwolf818.calltamer.ui.addblock.DurationSelectionScreen
import pl.septicwolf818.calltamer.ui.blockdetail.BlockDetailScreen
import pl.septicwolf818.calltamer.ui.history.HistoryScreen
import pl.septicwolf818.calltamer.ui.home.HomeScreen
import pl.septicwolf818.calltamer.ui.onboarding.OnboardingScreen
import pl.septicwolf818.calltamer.ui.settings.SettingsScreen

val NavHostController.canGoBack: Boolean
    get() = this.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED

fun NavHostController.safePopBackStack() {
    if (canGoBack) {
        popBackStack()
    }
}

@Composable
fun CallTamerNavHost(
    navController: NavHostController,
    startDestination: String,
    onOnboardingComplete: () -> Unit = {}
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    onOnboardingComplete()
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAddBlock = { navController.navigate(Screen.AddBlock.route) },
                onNavigateToBlockDetail = { ruleId ->
                    navController.navigate(Screen.BlockDetail.createRoute(ruleId))
                },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.AddBlock.route) {
            AddBlockScreen(
                onNumberSelected = { number ->
                    navController.navigate(Screen.AddBlockDuration.createRoute(number))
                },
                onNavigateBack = { navController.safePopBackStack() }
            )
        }

        composable(
            route = Screen.AddBlockDuration.route,
            arguments = listOf(
                navArgument("number") { type = NavType.StringType },
                navArgument("ruleId") { type = NavType.LongType; defaultValue = -1L }
            )
        ) { backStackEntry ->
            val number = backStackEntry.arguments?.getString("number") ?: return@composable
            val ruleId = backStackEntry.arguments?.getLong("ruleId")
            val isEdit = ruleId != null && ruleId > 0
            DurationSelectionScreen(
                phoneNumber = number,
                existingRuleId = ruleId?.takeIf { it > 0 },
                onBlockCreated = {
                    if (isEdit) {
                        navController.safePopBackStack()
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                },
                onNavigateBack = { navController.safePopBackStack() }
            )
        }

        composable(
            route = Screen.BlockDetail.route,
            arguments = listOf(
                navArgument("ruleId") { type = NavType.LongType },
                navArgument("confirmUnblock") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "calltamer://block/{ruleId}?confirmUnblock={confirmUnblock}" }
            )
        ) { backStackEntry ->
            val ruleId = backStackEntry.arguments?.getLong("ruleId") ?: return@composable
            val confirmUnblock = backStackEntry.arguments?.getBoolean("confirmUnblock") ?: false
            BlockDetailScreen(
                ruleId = ruleId,
                confirmUnblock = confirmUnblock,
                onChangeDuration = { number, id ->
                    navController.navigate(Screen.AddBlockDuration.createRoute(number, id))
                },
                onNavigateBack = { navController.safePopBackStack() }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onBlockNumber = { number ->
                    navController.navigate(Screen.AddBlockDuration.createRoute(number))
                },
                onNavigateBack = { navController.safePopBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onNavigateBack = { navController.safePopBackStack() })
        }
    }
}
