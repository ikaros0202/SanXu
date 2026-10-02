package com.sanxu.domain.repository

import com.sanxu.domain.pick.*
import kotlinx.coroutines.flow.Flow

enum class PlanFailure { INVALID_INPUT, INVALID_ISSUE, ALREADY_DRAWN, CONFLICT, STORAGE }
sealed interface PlanWriteResult {
    data class Saved(val entry: PlanEntry, val repeatedNumbers: Int, val alreadyApplied: Boolean = false) : PlanWriteResult
    data object Removed : PlanWriteResult
    data class Failed(val reason: PlanFailure) : PlanWriteResult
}
data class SavedMethod(val id: String, val title: String, val input: PickInput, val window: Int)
interface PlanRepository {
    val entries: Flow<List<PlanEntry>>
    val methods: Flow<List<SavedMethod>>
    suspend fun draft(key: String): PickDraft?
    suspend fun saveDraft(key: String, draft: PickDraft): Boolean
    suspend fun save(entry: PlanEntry, expectedRevision: Int? = null): PlanWriteResult
    suspend fun remove(id: String, expectedRevision: Int): PlanWriteResult
    suspend fun saveMethod(method: SavedMethod): Boolean
    suspend fun removeMethod(id: String): Boolean
}
