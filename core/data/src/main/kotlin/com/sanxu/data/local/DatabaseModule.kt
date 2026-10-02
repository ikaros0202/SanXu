package com.sanxu.data.local

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton fun database(@ApplicationContext context:Context):SanxuDatabase = Room.databaseBuilder(context,SanxuDatabase::class.java,SanxuDatabase.DATABASE_NAME).createFromAsset(SanxuDatabase.ASSET_PATH).setDriver(BundledSQLiteDriver()).build()
    @Provides fun draws(db:SanxuDatabase)=db.drawDao()
    @Provides fun metadata(db:SanxuDatabase)=db.syncMetadataDao()
    @Provides fun live(db:SanxuDatabase)=db.liveContentDao()
    @Provides fun announcements(db:SanxuDatabase)=db.yunnanAnnouncementDao()
    @Provides fun plans(db:SanxuDatabase)=db.planDao()
}
