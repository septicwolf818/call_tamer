package pl.septicwolf818.calltamer.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import pl.septicwolf818.calltamer.R
import pl.septicwolf818.calltamer.domain.ExpiryCalculator

@Composable
fun formatRemaining(expiresAt: Long?, now: Long): String {
    val calculator = ExpiryCalculator()
    if (expiresAt == null) return stringResource(R.string.remaining_permanent)
    val duration = calculator.remainingDuration(expiresAt, now)
        ?: return stringResource(R.string.remaining_permanent)
    if (duration.isZero || duration.isNegative) return stringResource(R.string.remaining_expiring)

    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60

    return when {
        hours > 0 && minutes > 0 -> stringResource(R.string.remaining_hours_minutes, hours, minutes)
        hours > 0 -> stringResource(R.string.remaining_hours, hours)
        minutes > 0 -> stringResource(R.string.remaining_minutes, minutes)
        else -> stringResource(R.string.remaining_less_than_minute)
    }
}
