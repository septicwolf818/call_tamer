package pl.septicwolf818.calltamer.domain

import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpiryCalculator @Inject constructor() {

    fun remainingDuration(expiresAt: Long?, now: Long): Duration? {
        if (expiresAt == null) return null
        val remaining = expiresAt - now
        return if (remaining <= 0) Duration.ZERO else Duration.ofMillis(remaining)
    }
}
