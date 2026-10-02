package com.sanxu.domain.repository

import com.sanxu.core.model.YunnanAnnouncement
import kotlinx.coroutines.flow.Flow

interface FeedRepository {
    val announcement: Flow<YunnanAnnouncement?>
    fun announcement(issue: String): Flow<YunnanAnnouncement?>
    suspend fun refresh(manual: Boolean): SyncResult
    suspend fun refreshIssue(issue: String)
}
