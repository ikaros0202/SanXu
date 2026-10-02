package com.sanxu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanxu.core.model.*
import com.sanxu.domain.pick.*
import com.sanxu.domain.filter.*
import com.sanxu.domain.repository.*
import com.sanxu.domain.livecontent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

data class SaveFeedback(val entry: PlanEntry, val repeated: Int, val previous: PlanEntry?)
data class EditorState(
    val draft: PickDraft = PickDraft(),
    val ready: Boolean = false,
    val saving: Boolean = false,
    val message: String? = null,
    val feedback: SaveFeedback? = null,
)

@HiltViewModel
class WorkspaceViewModel @Inject constructor(
    private val drawsRepository: DrawRepository,
    private val plans: PlanRepository,
    val live: LiveContentRepository,
    private val feed: FeedRepository,
    private val preferences: SettingsStore,
): ViewModel() {
    val draws = drawsRepository.allDrawsAscending.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val metadata = drawsRepository.syncMetadata.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val entries = plans.entries.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val methods = plans.methods.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val settings = preferences.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())
    val trial = live.trialNumber.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val trials = live.trialNumbers.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val trialState = live.trialRefreshState.stateIn(viewModelScope, SharingStarted.Eagerly, LiveContentRefreshState.Idle)
    val announcement = feed.announcement.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val documents = live.caibaoDocuments.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val caibaoState = live.caibaoRefreshState.stateIn(viewModelScope, SharingStarted.Eagerly, LiveContentRefreshState.Idle)
    private val _editor = MutableStateFlow(EditorState())
    val editor = _editor.asStateFlow()
    private val _refreshing = MutableStateFlow(false)
    val refreshing = _refreshing.asStateFlow()
    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage = _syncMessage.asStateFlow()
    // All draft and plan mutations are ordered. A slow disk write cannot replace a newer draft.
    private val work = Channel<suspend () -> Unit>(Channel.UNLIMITED)
    private var requestId: String? = null

    init {
        viewModelScope.launch { for (action in work) action() }
        work.trySend {
            val draft = plans.draft(PickMethod.SINGLE.name) ?: PickDraft()
            _editor.value = EditorState(draft, ready = true)
        }
        viewModelScope.launch { live.importBundledTrialSeed() }
    }

    fun foreground() = refresh(manual = false)

    fun refresh(manual: Boolean = true) {
        if (_refreshing.value) return
        _refreshing.value = true
        viewModelScope.launch {
            try {
                val result = feed.refresh(manual)
                _syncMessage.value = when(result) {
                    is SyncResult.Failed -> "暂时无法更新，已保留本地开奖。可稍后重试。"
                    is SyncResult.Updated -> if(manual) "开奖已更新" else null
                    else -> if(manual) "本地开奖已就绪" else null
                }
                val current = _editor.value
                if (current.draft.issue.isBlank()) {
                    val suggested = suggestedIssue()
                    if (suggested.isNotBlank()) updateDraft { it.copy(issue=suggested) }
                }
            } finally { _refreshing.value = false }
        }
        viewModelScope.launch { live.refreshTrial(if(manual) LiveRefreshTrigger.MANUAL else LiveRefreshTrigger.AUTO_FOREGROUND) }
    }

    fun suggestedIssue(): String {
        val last=draws.value.lastOrNull() ?: return ""
        val today=LocalDate.now(ZoneId.of("Asia/Shanghai"))
        val date=runCatching { LocalDate.parse(last.drawDate) }.getOrNull() ?: return ""
        val suffix=last.issue.takeLast(3).toIntOrNull() ?: return ""
        return if(date.year==today.year && !date.isBefore(today.minusDays(7)) && suffix<366)
            last.issue.take(4)+(suffix+1).toString().padStart(3,'0') else ""
    }

    fun updateDraft(change: (PickDraft)->PickDraft) {
        if (!_editor.value.ready || _editor.value.saving) return
        val updated=change(_editor.value.draft)
        _editor.update { it.copy(draft=updated,message=null,feedback=null) }
        requestId=null
        work.trySend { if(!plans.saveDraft(updated.input.method.name,updated)) message("草稿保存失败，请保持应用打开后重试") }
    }
    fun updateInput(change:(PickInput)->PickInput)=updateDraft { it.copy(input=change(it.input)) }

    fun chooseMethod(method:PickMethod) {
        if (!_editor.value.ready || _editor.value.saving) return
        val issue=_editor.value.draft.issue
        _editor.update { it.copy(ready=false) }
        work.trySend {
            val saved=plans.draft(method.name)
            val draft=saved ?: PickDraft(input=PickInput(method=method,play=if(method==PickMethod.GROUP) PlayType.GROUP3 else PlayType.STRAIGHT),issue=issue.ifBlank(::suggestedIssue))
            requestId=null
            _editor.value=EditorState(draft,ready=true)
        }
    }

    fun save() {
        val state=_editor.value
        if(!state.ready || state.saving) return
        val draft=state.draft
        val generated=PickEngine.generate(draft.input)
        if(generated.problem!=null) { message("请先完成选号"); return }
        val id=draft.editingId ?: requestId ?: UUID.randomUUID().toString().also { requestId=it }
        _editor.update { it.copy(saving=true,message=null) }
        work.trySend {
            val previous=entries.value.firstOrNull { it.id==draft.editingId }
            val entry=PlanEntry(id,draft.issue,draft.input,generated.numbers,draft.multiplier,draft.note,System.currentTimeMillis())
            when(val result=plans.save(entry,draft.revision)) {
                is PlanWriteResult.Saved -> {
                    val savedDraft=draft.copy(editingId=result.entry.id,revision=result.entry.revision)
                    plans.saveDraft(draft.input.method.name,savedDraft)
                    _editor.value=EditorState(savedDraft,true,feedback=SaveFeedback(result.entry,result.repeatedNumbers,previous))
                }
                is PlanWriteResult.Failed -> _editor.update { it.copy(saving=false,message=failureLabel(result.reason)) }
                else -> _editor.update { it.copy(saving=false) }
            }
        }
    }

    fun edit(entry:PlanEntry, copy:Boolean=false) {
        if(_editor.value.saving) return
        val draft=PickDraft(entry.input,if(copy) suggestedIssue() else entry.issue,entry.multiplier,if(copy) null else entry.id,if(copy) null else entry.revision,entry.note)
        _editor.value=EditorState(draft,true)
        requestId=null
        work.trySend { plans.saveDraft(draft.input.method.name,draft) }
    }
    fun newEntry() = updateDraft { it.copy(input=PickInput(method=it.input.method,play=it.input.play),editingId=null,revision=null,note="") }
    fun useTrendDigit(position:Int,digit:Int) {
        if(position !in 0..2||digit !in 0..9||_editor.value.saving) return
        val issue=_editor.value.draft.issue
        _editor.update { it.copy(ready=false) }
        work.trySend {
            val source=plans.draft(PickMethod.POSITION.name) ?: PickDraft(PickInput(method=PickMethod.POSITION),issue.ifBlank(::suggestedIssue))
            val positions=source.input.positions.mapIndexed { index,set->if(index==position)set+digit else set }
            val draft=source.copy(input=source.input.copy(positions=positions,excluded=emptySet()),editingId=null,revision=null)
            requestId=null
            plans.saveDraft(PickMethod.POSITION.name,draft)
            _editor.value=EditorState(draft,true)
        }
    }
    fun dismissFeedback() { _editor.update { it.copy(feedback=null) } }
    fun dismissMessage() { _editor.update { it.copy(message=null) } }
    fun dismissSync() { _syncMessage.value=null }

    fun remove(entry:PlanEntry) {
        work.trySend {
            when(val result=plans.remove(entry.id,entry.revision)) {
                is PlanWriteResult.Failed -> message(failureLabel(result.reason))
                else -> {
                    if(_editor.value.draft.editingId==entry.id) updateDraft { it.copy(editingId=null,revision=null) }
                    message("已删除该选号记录，其余记录保持不变")
                }
            }
        }
    }

    fun undo(feedback:SaveFeedback) {
        if(_editor.value.saving) return
        _editor.update { it.copy(saving=true,feedback=null) }
        work.trySend {
            val previous=feedback.previous
            val result=if(previous==null) plans.remove(feedback.entry.id,feedback.entry.revision)
                else plans.save(previous,feedback.entry.revision)
            when(result) {
                is PlanWriteResult.Failed -> _editor.update { it.copy(saving=false,message=failureLabel(result.reason)) }
                else -> {
                    val restored=(result as? PlanWriteResult.Saved)?.entry
                    val draft=if(restored==null) _editor.value.draft.copy(editingId=null,revision=null)
                        else PickDraft(restored.input,restored.issue,restored.multiplier,restored.id,restored.revision,restored.note)
                    plans.saveDraft(draft.input.method.name,draft)
                    _editor.update { it.copy(draft=draft,saving=false,message="已撤销这次保存") }
                }
            }
        }
    }
    fun saveMethod(title:String, window:Int) {
        val input=_editor.value.draft.input
        if(title.isBlank()) { message("请填写模板名称"); return }
        work.trySend { message(if(plans.saveMethod(SavedMethod(UUID.randomUUID().toString(),title.trim(),input,window))) "模板已保存" else "模板保存失败，请重试") }
    }
    fun applyMethod(method:SavedMethod) {
        val draft=PickDraft(method.input,_editor.value.draft.issue.ifBlank(::suggestedIssue))
        _editor.value=EditorState(draft,true)
        requestId=null
        work.trySend { plans.saveDraft(draft.input.method.name,draft) }
    }
    fun deleteMethod(id:String) { work.trySend { if(!plans.removeMethod(id)) message("模板删除失败，请重试") } }
    fun setTheme(theme:ThemeChoice) { viewModelScope.launch { preferences.setTheme(theme) } }
    fun setNotice(enabled:Boolean) { viewModelScope.launch { preferences.setReplayNotice(enabled) } }
    fun refreshTrial() { viewModelScope.launch { live.refreshTrial(LiveRefreshTrigger.MANUAL) } }
    fun refreshReport(issue:String?=null) { viewModelScope.launch {
        if(issue==null) live.refreshCaibao(LiveRefreshTrigger.MANUAL) else live.refreshCaibaoIssue(issue)
    } }
    fun openReports() { viewModelScope.launch { live.refreshCaibao(LiveRefreshTrigger.CAIBAO_VISIBLE) } }
    suspend fun announcement(issue:String):YunnanAnnouncement? {
        val existing=feed.announcement(issue).first()
        if(existing!=null) return existing
        feed.refreshIssue(issue)
        return feed.announcement(issue).first()
    }
    private fun message(value:String) { _editor.update { it.copy(message=value) } }
}

fun failureLabel(reason:PlanFailure)=when(reason) {
    PlanFailure.INVALID_ISSUE -> "请填写有效的七位期号，例如 2026270"
    PlanFailure.ALREADY_DRAWN -> "这一期已经开奖，不能更改。可以复制到新一期。"
    PlanFailure.CONFLICT -> "记录已发生变化，请从方案重新打开后再操作"
    PlanFailure.INVALID_INPUT -> "号码或倍数无效，请检查输入"
    PlanFailure.STORAGE -> "保存失败，输入已保留，请重试"
}
