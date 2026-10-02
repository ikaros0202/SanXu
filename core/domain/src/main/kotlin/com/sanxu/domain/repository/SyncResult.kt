package com.sanxu.domain.repository
enum class SyncTrigger { AUTO, MANUAL }

sealed interface SyncResult {
    data class Updated(
        val added: Int,
        val corrected: Int,
        val unchanged: Int,
        val latestIssue: String?,
    ) : SyncResult

    data class Failed(val failureType: String) : SyncResult
    data object Throttled : SyncResult
    data object AlreadyRunning : SyncResult
}
