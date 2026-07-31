package pl.septicwolf818.calltamer.domain

import android.content.Context
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactNameResolver @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun resolveName(number: String): String? = withContext(Dispatchers.IO) {
        if (number.isBlank()) return@withContext null
        try {
            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            queryFirst(
                projection,
                "${ContactsContract.CommonDataKinds.Phone.NUMBER} = ?",
                arrayOf(number)
            ) ?: PhoneNumberUtils.normalizeNumber(number).takeIf { it.isNotBlank() }?.let { normalized ->
                queryFirst(
                    projection,
                    "${ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER} = ?",
                    arrayOf(normalized)
                )
            }
        } catch (_: SecurityException) {
            null
        }
    }

    private fun queryFirst(projection: Array<String>, selection: String, args: Array<String>): String? {
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            args,
            null
        )?.use { c ->
            if (c.moveToFirst()) {
                val name = c.getString(0)
                if (!name.isNullOrBlank()) return name
            }
        }
        return null
    }
}
