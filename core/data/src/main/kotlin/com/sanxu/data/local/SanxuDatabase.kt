package com.sanxu.data.local

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(entities=[DrawEntity::class,SyncMetadataEntity::class,TrialNumberEntity::class,CaibaoDocumentEntity::class,LiveContentRefreshMetadataEntity::class,YunnanAnnouncementEntity::class,PlanEntryEntity::class,DraftEntity::class,MethodEntity::class],version=1,exportSchema=true)
abstract class SanxuDatabase:RoomDatabase() {
    abstract fun drawDao():DrawDao
    abstract fun syncMetadataDao():SyncMetadataDao
    abstract fun liveContentDao():LiveContentDao
    abstract fun yunnanAnnouncementDao():YunnanAnnouncementDao
    abstract fun planDao():PlanDao
    companion object { const val DATABASE_NAME="sanxu.db"; const val ASSET_PATH="database/sanxu.db" }
}
