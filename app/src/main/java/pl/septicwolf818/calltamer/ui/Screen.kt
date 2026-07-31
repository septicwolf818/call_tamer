package pl.septicwolf818.calltamer.ui

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object AddBlock : Screen("addBlock")
    data object AddBlockDuration : Screen("addBlock/duration/{number}?ruleId={ruleId}") {
        fun createRoute(number: String) = "addBlock/duration/$number"
        fun createRoute(number: String, ruleId: Long) = "addBlock/duration/$number?ruleId=$ruleId"
    }
    data object BlockDetail : Screen("blockDetail/{ruleId}?confirmUnblock={confirmUnblock}") {
        fun createRoute(ruleId: Long) = "blockDetail/$ruleId"
        fun createRoute(ruleId: Long, confirmUnblock: Boolean) =
            "blockDetail/$ruleId?confirmUnblock=$confirmUnblock"
    }
    data object History : Screen("history")
    data object Settings : Screen("settings")
}
