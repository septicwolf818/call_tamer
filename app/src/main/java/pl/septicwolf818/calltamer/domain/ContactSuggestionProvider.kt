package pl.septicwolf818.calltamer.domain

import android.content.Context
import android.provider.ContactsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class ContactSuggestion(
    val name: String?,
    val number: String
)

@Singleton
class ContactSuggestionProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun search(query: String, limit: Int = 5): List<ContactSuggestion> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            val like = "%${query.trim()}%"
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )
            val selection = "(" +
                "${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ? OR " +
                "${ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER} LIKE ?" +
                ")"
            val suggestions = mutableListOf<ContactSuggestion>()
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                selection,
                arrayOf(like, like),
                null
            )?.use { c ->
                val nameIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (c.moveToNext() && suggestions.size < limit) {
                    val number = c.getString(numberIdx)?.trim()
                    if (!number.isNullOrBlank()) {
                        suggestions.add(
                            ContactSuggestion(
                                name = c.getString(nameIdx),
                                number = number
                            )
                        )
                    }
                }
            }
            suggestions
        } catch (_: SecurityException) {
            emptyList()
        }
    }
}
