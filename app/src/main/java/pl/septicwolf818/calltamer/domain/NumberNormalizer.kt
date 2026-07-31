package pl.septicwolf818.calltamer.domain

import android.telephony.PhoneNumberUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NumberNormalizer @Inject constructor() {

    /**
     * Normalizes a phone number to a consistent digits-only string for matching.
     * Strips all non-digit characters including +, -, (, ), spaces, etc.
     * This produces a format like "14155551234" from any of:
     *   +1 (415) 555-1234
     *   14155551234
     *   (415) 555-1234
     */
    fun normalize(number: String): String {
        return PhoneNumberUtils.normalizeNumber(number)
    }

    /**
     * Compares two numbers after normalization.
     */
    fun matches(a: String, b: String): Boolean {
        return normalize(a) == normalize(b)
    }
}
