package com.sanxu.data.repository
import com.sanxu.domain.repository.*

import com.sanxu.core.model.DrawRecord
import com.sanxu.data.local.DrawDao
import com.sanxu.data.local.SyncMetadataDao
import com.sanxu.data.local.SyncMetadataEntity
import com.sanxu.data.mapper.toRecord
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class DefaultDrawRepository @Inject constructor(
    private val drawDao: DrawDao,
    syncMetadataDao: SyncMetadataDao,
    private val syncCoordinator: SyncCoordinator,
) : DrawRepository {
    override val latestDraw: Flow<DrawRecord?> = drawDao.observeLatest().map { it?.toRecord() }
    override val allDrawsAscending: Flow<List<DrawRecord>> =
        drawDao.observeAllAscending().map { entities -> entities.map { it.toRecord() } }
    override val syncMetadata: Flow<DrawSyncMetadata?> =
        syncMetadataDao.observe().map { it?.toDrawSyncMetadata() }

    override fun observeRecent(limit: Int): Flow<List<DrawRecord>> {
        require(limit > 0) { "Recent draw limit must be positive" }
        return drawDao.observeRecent(limit).map { entities -> entities.map { it.toRecord() } }
    }

    override fun observe(query: DrawQuery): Flow<List<DrawRecord>> = when (query) {
        is DrawQuery.Recent -> observeRecent(query.limit)
        is DrawQuery.Issue -> drawDao.observeByIssue(query.issue)
            .map { entity -> listOfNotNull(entity?.toRecord()) }
        is DrawQuery.Year -> drawDao.observeByYear(query.year)
            .map { entities -> entities.map { it.toRecord() } }
        is DrawQuery.DateRange -> drawDao.observeByDateRange(query.startDate, query.endDate)
            .map { entities -> entities.map { it.toRecord() } }
    }

    override suspend fun refresh(): SyncResult = syncCoordinator.sync(SyncTrigger.MANUAL)

    override suspend fun syncOnForeground(): SyncResult = syncCoordinator.sync(SyncTrigger.AUTO)
}

internal fun SyncMetadataEntity.toDrawSyncMetadata(): DrawSyncMetadata = DrawSyncMetadata(
    lastSuccessEpochMillis = lastSuccessEpochMillis,
    latestIssue = latestIssue,
    lastFailureType = lastFailureType,
    correctedIssues = runCatching {
        kotlinx.serialization.json.Json.decodeFromString<List<String>>(correctedIssuesJson).toSet()
    }.getOrDefault(emptySet()),
)
