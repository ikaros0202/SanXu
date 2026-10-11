package com.sanxu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.sanxu.domain.attributes.DrawAttributes
import com.sanxu.domain.livecontent.LiveContentRefreshState
import com.sanxu.domain.livecontent.LiveRefreshTrigger
import com.sanxu.domain.livecontent.TrialVisibility
import com.sanxu.domain.pick.PickEngine
import com.sanxu.ui.NumberFont
import com.sanxu.ui.WorkspacePanel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable fun HomePage(model: WorkspaceViewModel, onHistory: (String) -> Unit, onPick: () -> Unit, onPlan: () -> Unit) {
    val draws by model.draws.collectAsStateWithLifecycle()
    val entries by model.entries.collectAsStateWithLifecycle()
    val editor by model.editor.collectAsStateWithLifecycle()
    val candidateTrial by model.trial.collectAsStateWithLifecycle()
    val trialState by model.trialState.collectAsStateWithLifecycle()
    val metadata by model.metadata.collectAsStateWithLifecycle()
    val refreshing by model.refreshing.collectAsStateWithLifecycle()
    val settings by model.settings.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val now by produceState(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")), lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (isActive) {
                value = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))
                model.live.refreshTrial(LiveRefreshTrigger.HOME_VISIBLE)
                delay(30_000)
            }
        }
    }
    val trial = TrialVisibility.visible(candidateTrial, now)
    val latest = draws.lastOrNull()
    val drawnIssues = remember(draws) { draws.mapTo(hashSetOf()) { it.issue } }
    val pending = remember(entries, drawnIssues) { entries.filter { it.issue !in drawnIssues }.groupBy { it.issue } }
    val currentIssue = editor.draft.issue.takeIf { it in pending } ?: pending.keys.maxOrNull()
    val summary = remember(pending, currentIssue) { PickEngine.merge(currentIssue?.let { pending[it] }.orEmpty()) }
    val checked = remember(entries, drawnIssues) { entries.map { it.issue }.distinct().count { it in drawnIssues } }
    val trialDescription = stringResource(R.string.home_trial_refresh)
    val detailsDescription = stringResource(R.string.home_draw_details)
    val allDescription = stringResource(R.string.home_all_draws)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        WorkspacePanel {
            if (latest == null) {
                val failed = metadata?.lastFailureType != null
                EmptyPanel(stringResource(if (failed) R.string.home_retry_title else R.string.home_loading_title),
                    stringResource(if (failed) R.string.home_retry_body else R.string.home_loading_body),
                    if (failed) stringResource(R.string.home_retry) else null, model::refresh)
            } else {
                val attributes = remember(latest) { DrawAttributes.calculate(latest.number) }
                Row(Modifier.fillMaxWidth().height(48.dp).padding(start = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.studio_issue, latest.issue), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(8.dp))
                    SmallLabel(latest.drawDate.takeLast(5), Modifier.weight(1f))
                    if (refreshing) CircularProgressIndicator(Modifier.padding(14.dp).size(20.dp), strokeWidth = 2.dp)
                    else IconButton(onClick = model::refresh) { Icon(Icons.Outlined.Refresh, stringResource(R.string.home_refresh), Modifier.size(18.dp)) }
                }
                Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1.5f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(latest.number.digits.joinToString(" "), fontFamily = NumberFont, fontSize = 26.sp,
                                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            TextButton(onClick = { onHistory(latest.issue) }, modifier = Modifier.height(48.dp).testTag("latest_draw_details")
                                .semantics { contentDescription = detailsDescription }, contentPadding = PaddingValues(horizontal = 6.dp)) {
                                Text(stringResource(R.string.studio_details), fontSize = 11.sp)
                                Icon(Icons.Outlined.ChevronRight, null, Modifier.size(14.dp))
                            }
                        }
                        SmallLabel(stringResource(R.string.home_attributes, attributes.sum, attributes.span, shapeLabel(attributes.groupShape)))
                    }
                    VerticalDivider(Modifier.height(48.dp).padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    Column(Modifier.weight(1f).heightIn(min = 64.dp)
                        .clickable(enabled = trialState != LiveContentRefreshState.Refreshing, onClick = model::refreshTrial)
                        .semantics { contentDescription = trialDescription }, verticalArrangement = Arrangement.Center) {
                        SmallLabel(stringResource(R.string.home_trial))
                        Text(trial?.number?.toList()?.joinToString(" ") ?: "— — —", fontFamily = NumberFont, fontSize = 18.sp,
                            fontWeight = FontWeight.Medium)
                        SmallLabel(when {
                            trialState == LiveContentRefreshState.Refreshing -> stringResource(R.string.home_trial_refreshing)
                            trialState is LiveContentRefreshState.Failed -> stringResource(R.string.home_trial_retry)
                            trial != null -> stringResource(R.string.home_trial_period, trial.issue)
                            else -> stringResource(R.string.home_trial_waiting)
                        })
                    }
                }
                if (metadata?.lastFailureType != null) {
                    val last = metadata?.lastSuccessEpochMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ofPattern("MM-dd HH:mm")) }
                    SmallLabel(stringResource(R.string.home_sync_failed,
                        if (last != null) stringResource(R.string.home_last_success, last) else stringResource(R.string.home_kept_draw, latest.drawDate)),
                        Modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp))
                }
            }
        }
        if (draws.isNotEmpty()) WorkspacePanel {
            Row(Modifier.fillMaxWidth().height(48.dp).padding(start = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.home_recent), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { onHistory("") }, modifier = Modifier.height(48.dp).semantics { contentDescription = allDescription }) {
                    Text(stringResource(R.string.home_all)); Icon(Icons.Outlined.ChevronRight, null, Modifier.size(16.dp))
                }
            }
            Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
                Row(Modifier.fillMaxWidth().height(24.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    SmallLabel(stringResource(R.string.home_issue_column), Modifier.weight(1.25f))
                    SmallLabel(stringResource(R.string.home_number_column), Modifier.weight(1f))
                    SmallLabel(stringResource(R.string.home_sum_column), Modifier.width(40.dp))
                    SmallLabel(stringResource(R.string.home_span_column), Modifier.width(32.dp))
                }
            }
            draws.asReversed().drop(1).take(10).forEach { draw ->
                val attributes = remember(draw) { DrawAttributes.calculate(draw.number) }
                // Compact rows are read-only. The archive and latest-detail actions retain full touch targets.
                Row(Modifier.fillMaxWidth().height(30.dp).testTag("recent_${draw.issue}").padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(draw.issue, Modifier.weight(1.25f), fontFamily = NumberFont, fontSize = 13.sp)
                    Text(draw.number.digits.joinToString(" "), Modifier.weight(1f), fontFamily = NumberFont, fontSize = 16.sp,
                        fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                    Text(attributes.sum.toString(), Modifier.width(40.dp), fontFamily = NumberFont, fontSize = 13.sp)
                    Text(attributes.span.toString(), Modifier.width(32.dp), fontFamily = NumberFont, fontSize = 13.sp)
                }
                HorizontalDivider(Modifier.padding(horizontal = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .55f))
            }
        }
        WorkspacePanel {
            Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f).heightIn(min = 48.dp).clickable(onClick = onPlan).padding(horizontal = 4.dp), verticalArrangement = Arrangement.Center) {
                    Text(stringResource(R.string.home_current_plan), style = MaterialTheme.typography.titleMedium)
                    SmallLabel(when {
                        currentIssue != null -> stringResource(R.string.home_plan_summary, currentIssue, summary.unitCount, summary.amountYuan)
                        settings.showReplayNotice && checked > 0 -> stringResource(R.string.home_plan_replay, checked)
                        else -> stringResource(R.string.home_plan_empty)
                    })
                }
                Button(onClick = onPick, modifier = Modifier.height(48.dp).testTag("home_start_pick"),
                    shape = MaterialTheme.shapes.small, contentPadding = PaddingValues(horizontal = 12.dp)) {
                    Text(stringResource(if (currentIssue == null) R.string.home_start_pick else R.string.home_continue_pick))
                    Icon(Icons.Outlined.ChevronRight, null, Modifier.size(16.dp))
                }
            }
        }
    }
}
