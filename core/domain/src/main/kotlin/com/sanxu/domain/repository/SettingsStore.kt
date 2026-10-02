package com.sanxu.domain.repository
import kotlinx.coroutines.flow.Flow
enum class ThemeChoice { SYSTEM, LIGHT, DARK }
data class AppSettings(val theme:ThemeChoice=ThemeChoice.SYSTEM,val showReplayNotice:Boolean=true)
interface SettingsStore {
    val settings:Flow<AppSettings>
    suspend fun setTheme(theme:ThemeChoice)
    suspend fun setReplayNotice(enabled:Boolean)
}
