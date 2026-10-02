package com.sanxu.data.repository
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.*
import com.sanxu.domain.repository.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.map
private val Context.sanxuPreferences by preferencesDataStore("sanxu-settings")
class LocalSettings @Inject constructor(@ApplicationContext private val context:Context):SettingsStore {
    private val themeKey=stringPreferencesKey("theme")
    private val noticeKey=booleanPreferencesKey("notice")
    override val settings=context.sanxuPreferences.data.map { AppSettings(runCatching { ThemeChoice.valueOf(it[themeKey]?:"SYSTEM") }.getOrDefault(ThemeChoice.SYSTEM),it[noticeKey]?:true) }
    override suspend fun setTheme(theme:ThemeChoice) { context.sanxuPreferences.edit { it[themeKey]=theme.name } }
    override suspend fun setReplayNotice(enabled:Boolean) { context.sanxuPreferences.edit { it[noticeKey]=enabled } }
}
