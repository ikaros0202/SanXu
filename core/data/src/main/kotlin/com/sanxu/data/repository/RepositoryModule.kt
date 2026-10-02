package com.sanxu.data.repository
import com.sanxu.domain.repository.*

import android.content.Context
import com.sanxu.data.file.CaibaoFileStore
import com.sanxu.data.file.BundledTrialSeedDataSource
import com.sanxu.data.file.TrialSeedDataSource
import com.sanxu.data.remote.OfficialDrawDataSource
import com.sanxu.data.remote.OfficialFc3dDataSource
import com.sanxu.data.remote.CaibaoDataSource
import com.sanxu.data.remote.CaibaTrialDataSource
import com.sanxu.data.remote.Cz89CaibaoDataSource
import com.sanxu.data.remote.TrialDataSource
import com.sanxu.data.remote.YunnanAnnouncementDataSource
import com.sanxu.data.remote.YunnanOfficialDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.time.Clock
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient()

    @Provides
    @Singleton
    fun provideOfficialDrawDataSource(client: OkHttpClient): OfficialDrawDataSource =
        OfficialFc3dDataSource(client)

    @Provides
    @Singleton
    fun provideYunnanAnnouncementDataSource(client: OkHttpClient): YunnanOfficialDataSource =
        YunnanAnnouncementDataSource(client)

    @Provides
    @Singleton
    fun provideTrialDataSource(client: OkHttpClient): TrialDataSource = CaibaTrialDataSource(client)

    @Provides
    @Singleton
    fun provideTrialSeedDataSource(source: BundledTrialSeedDataSource): TrialSeedDataSource = source

    @Provides
    @Singleton
    fun provideCaibaoDataSource(client: OkHttpClient): CaibaoDataSource = Cz89CaibaoDataSource(client)

    @Provides
    @Singleton
    fun provideCaibaoFileStore(@ApplicationContext context: Context): CaibaoFileStore =
        CaibaoFileStore(File(context.filesDir, "live-content/caibao"))

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemUTC()

    @Provides
    @Singleton
    fun provideDrawRepository(repository: DefaultDrawRepository): DrawRepository = repository

    @Provides
    @Singleton
    fun provideYunnanAnnouncementRepository(
        repository: DefaultYunnanAnnouncementRepository,
    ): YunnanAnnouncementRepository = repository

    @Provides
    @Singleton
    fun provideLiveContentRepository(
        repository: DefaultLiveContentRepository,
    ): LiveContentRepository = repository

    @Provides
    @Singleton
    fun providePlanRepository(repository: LocalPlanRepository): PlanRepository = repository

    @Provides @Singleton
    fun provideSettingsStore(repository: LocalSettings): SettingsStore = repository

    @Provides @Singleton
    fun provideFeedRepository(repository: DefaultFeedRepository): FeedRepository = repository

    @Provides
    @Singleton
    fun provideSyncStore(store: RoomSyncStore): SyncStore = store

    @Provides
    @Singleton
    fun provideReplayRefresher(): ReplayRefresher = object: ReplayRefresher {
        override suspend fun refresh(issues: Set<String>) = Unit // Replays derive from the canonical draw Flow.
    }

    @Provides
    @Singleton
    fun provideTimeProvider(): TimeProvider = SystemTimeProvider
}
