package com.sanxu.data.repository

import com.sanxu.domain.repository.FeedRepository
import com.sanxu.domain.repository.SyncResult
import javax.inject.Inject

class DefaultFeedRepository @Inject constructor(private val announcements:YunnanAnnouncementRepository,private val coordinator:OfficialDataSyncCoordinator):FeedRepository {
    override val announcement=announcements.latestAnnouncement
    override fun announcement(issue:String)=announcements.observeByIssue(issue)
    override suspend fun refresh(manual:Boolean):SyncResult=coordinator.sync(if(manual) OfficialDataSyncTrigger.MANUAL else OfficialDataSyncTrigger.AUTO).drawResult
    override suspend fun refreshIssue(issue:String) { coordinator.syncIssue(issue) }
}
