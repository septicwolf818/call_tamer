package pl.septicwolf818.calltamer.domain

import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpiryCalculator @Inject constructor() {

    fun isExpired(expiresAt: Long?, now: Long): Boolean {
        return expiresAt != null && expiresAt <= now
    }

    fun remainingDuration(expiresAt: Long?, now: Long): Duration? {
        if (expiresAt == null) return null
        val remaining = expiresAt - now
        return if (remaining <= 0) Duration.ZERO else Duration.ofMillis(remaining)
    }

    fun expiryInstant(expiresAt: Long?): Instant? {
        return expiresAt?.let { Instant.ofEpochMilli(it) }
    }

    companion object {
        fun computeExpiresAt(durationMinutes: Long): Long {
            return Instant.now().plus(Duration.ofMinutes(durationMinutes)).toEpochMilli()
        }
    }
}
