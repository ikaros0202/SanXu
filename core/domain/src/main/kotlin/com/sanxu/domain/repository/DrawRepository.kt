package com.sanxu.domain.repository
import com.sanxu.core.model.DrawRecord
import kotlinx.coroutines.flow.Flow
data class DrawSyncMetadata(
    val lastSuccessEpochMillis: Long? = null,
    val latestIssue: String? = null,
    val lastFailureType: String? = null,
    val correctedIssues: Set<String> = emptySet(),
)

interface DrawRepository {
    val latestDraw: Flow<DrawRecord?>
    val allDrawsAscending: Flow<List<DrawRecord>>
    val syncMetadata: Flow<DrawSyncMetadata?>

    fun observeRecent(limit: Int): Flow<List<DrawRecord>>

    fun observe(query: DrawQuery): Flow<List<DrawRecord>>

    suspend fun refresh(): SyncResult

    suspend fun syncOnForeground(): SyncResult
}

sealed interface DrawQuery {
    data class Recent(val limit: Int) : DrawQuery {
        init {
            require(limit > 0)
        }
    }

    data class Issue(val issue: String) : DrawQuery {
        init {
            require(issue.matches(Regex("""\d{7}""")))
        }
    }

    data class Year(val year: String) : DrawQuery {
        init {
            require(year.matches(Regex("""\d{4}""")))
        }
    }

    data class DateRange(
        val startDate: String,
        val endDate: String,
    ) : DrawQuery {
        init {
            require(startDate.matches(ISO_DATE_REGEX))
            require(endDate.matches(ISO_DATE_REGEX))
            require(startDate <= endDate)
        }
    }

    private companion object {
        val ISO_DATE_REGEX = Regex("""\d{4}-\d{2}-\d{2}""")
    }
}
