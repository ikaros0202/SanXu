package com.sanxu.data.repository
import com.sanxu.domain.repository.*

import com.sanxu.core.model.YunnanAnnouncement
import com.sanxu.data.local.YunnanAnnouncementDao
import com.sanxu.data.mapper.toAnnouncement
import com.sanxu.data.mapper.toEntity
import com.sanxu.data.remote.YunnanAnnouncementDataResult
import com.sanxu.data.remote.YunnanOfficialDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex

typealias YunnanAnnouncementResult = YunnanAnnouncementDataResult

interface YunnanAnnouncementRepository {
    val latestAnnouncement: Flow<YunnanAnnouncement?>

    fun observeByIssue(issue: String): Flow<YunnanAnnouncement?>

    suspend fun refreshRecent(limit: Int = 5): YunnanAnnouncementResult

    suspend fun refreshIssue(issue: String): YunnanAnnouncementResult
}

@Singleton
class DefaultYunnanAnnouncementRepository @Inject constructor(
    private val dao: YunnanAnnouncementDao,
    private val remote: YunnanOfficialDataSource,
) : YunnanAnnouncementRepository {
    private val refreshMutex = Mutex()

    override val latestAnnouncement: Flow<YunnanAnnouncement?> = dao.observeLatest()
        .map { entity -> entity?.toDomainOrNull() }

    override fun observeByIssue(issue: String): Flow<YunnanAnnouncement?> =
        dao.observeByIssue(issue).map { entity -> entity?.toDomainOrNull() }

    override suspend fun refreshRecent(limit: Int): YunnanAnnouncementResult =
        lockAndRefresh { remote.fetchRecent(limit) }

    override suspend fun refreshIssue(issue: String): YunnanAnnouncementResult =
        lockAndRefresh { remote.fetchIssue(issue) }

    private suspend fun lockAndRefresh(
        fetch: suspend () -> YunnanAnnouncementResult,
    ): YunnanAnnouncementResult {
        refreshMutex.lock()
        return try {
            when (val result = fetch()) {
                is YunnanAnnouncementDataResult.Success -> {
                    val announcements = result.announcements
                    if (announcements.isEmpty()) {
                        YunnanAnnouncementDataResult.EmptyResponse
                    } else {
                        // The DAO method is a single Room transaction; no
                        // entity is written if mapping/validation fails.
                        if (dao.commitValidated(announcements.map { it.toEntity() })) {
                            result
                        } else {
                            YunnanAnnouncementDataResult.InvalidPayload(
                                "Announcement does not match local draw",
                            )
                        }
                    }
                }
                else -> result
            }
        } finally {
            refreshMutex.unlock()
        }
    }
}

private fun com.sanxu.data.local.YunnanAnnouncementEntity.toDomainOrNull(): YunnanAnnouncement? =
    runCatching { toAnnouncement() }.getOrNull()
