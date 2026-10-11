package com.sanxu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sanxu.domain.filter.PlayType
import com.sanxu.domain.pick.*
import com.sanxu.ui.*
import kotlinx.coroutines.delay

val primaryMethods = listOf(PickMethod.SINGLE, PickMethod.GROUP, PickMethod.DISTINCT, PickMethod.POSITION)
fun methodLabel(method: PickMethod) = when (method) {
    PickMethod.SINGLE -> "单选"; PickMethod.GROUP -> "组选"; PickMethod.DISTINCT -> "单选单"
    PickMethod.POSITION -> "位选"; PickMethod.FILTER -> "条件筛选"; PickMethod.DAN_TUO -> "胆拖"
}
fun playLabel(play: PlayType) = when (play) { PlayType.STRAIGHT -> "单选"; PlayType.GROUP3 -> "组三"; PlayType.GROUP6 -> "组六" }

@Composable fun PickPage(model: WorkspaceViewModel, onTools: () -> Unit, onDetails: () -> Unit) {
    val state by model.editor.collectAsStateWithLifecycle()
    val draft = state.draft
    val input = draft.input
    val enabled = state.ready && !state.saving
    val result = remember(input) { PickEngine.generate(input) }
    val base = remember(input) { PickEngine.generate(input.copy(excluded = emptySet())) }
    val candidates = remember(input, base) {
        when {
            input.method == PickMethod.SINGLE -> input.manual.distinct().sorted()
            input.method == PickMethod.GROUP && input.play == PlayType.GROUP3 -> base.numbers.sortedWith(
                compareBy<String> { number -> number.first { digit -> number.count { it == digit } == 2 } }.thenBy { it })
            else -> base.numbers
        }
    }
    var batch by rememberSaveable { mutableStateOf(false) }
    var help by rememberSaveable { mutableStateOf(false) }
    var issueEditor by rememberSaveable { mutableStateOf(false) }
    var multiplierEditor by rememberSaveable { mutableStateOf(false) }
    var singlePosition by rememberSaveable(input.method, draft.editingId) { mutableIntStateOf(-1) }
    // -2: closed, -1: one shared digit set, 0..2: a specific position.
    var digitPosition by rememberSaveable(input.method, draft.editingId) { mutableIntStateOf(-2) }
    var change by remember { mutableStateOf<DigitChange?>(null) }
    var changeSequence by remember { mutableIntStateOf(0) }
    LaunchedEffect(change?.sequence) { if (change != null) { delay(800); change = null } }
    val labels = positionLabels()
    val canCollect = SingleNumberEditor.canCollect(draft)
    val viewDescription = stringResource(R.string.studio_view_numbers, result.numbers.size)

    Column(Modifier.fillMaxSize()) {
        PageHeader(stringResource(R.string.studio_title)) {
            TextButton(onClick = { issueEditor = true }, enabled = enabled,
                modifier = Modifier.height(48.dp).testTag("issue_picker"), contentPadding = PaddingValues(horizontal = 4.dp)) {
                Text(if (draft.issue.isBlank()) stringResource(R.string.studio_choose_issue) else stringResource(R.string.studio_issue, draft.issue),
                    fontFamily = NumberFont, fontSize = 11.sp)
                Icon(Icons.Outlined.ExpandMore, null, Modifier.size(16.dp))
            }
            TextButton(onClick = onTools, enabled = enabled, modifier = Modifier.height(48.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text(stringResource(R.string.studio_tools))
            }
            IconButton(onClick = { help = true }) { Icon(Icons.Outlined.HelpOutline, stringResource(R.string.studio_help), Modifier.size(20.dp)) }
        }
        ChoiceRail(primaryMethods.map(::methodLabel), primaryMethods.indexOf(input.method), { model.chooseMethod(primaryMethods[it]) },
            Modifier.fillMaxWidth(), tags = primaryMethods.map { "method_${it.name}" }, enabled = enabled)
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 8.dp)) {
            if (!state.ready) Box(Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            else {
                if (input.method == PickMethod.GROUP) {
                    Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
                        ChoiceRail(listOf(playLabel(PlayType.GROUP3), playLabel(PlayType.GROUP6)), if (input.play == PlayType.GROUP3) 0 else 1,
                            { index -> model.updateInput {
                                val play = if (index == 0) PlayType.GROUP3 else PlayType.GROUP6
                                if (play == it.play) it else it.copy(play = play, manual = emptyList(), excluded = emptySet())
                            } },
                            Modifier.weight(1f), tags = listOf("group_GROUP3", "group_GROUP6"), enabled = enabled)
                        TextButton(onClick = { if (input.byNumber) model.updateInput { it.copy(byNumber = false, excluded = emptySet()) } else batch = true },
                            enabled = enabled, modifier = Modifier.height(48.dp)) {
                            Text(stringResource(if (input.byNumber) R.string.studio_digit_set else R.string.studio_by_number))
                        }
                    }
                }
                when (input.method) {
                    PickMethod.SINGLE -> {
                        SingleNumberRail(draft.singleEdit, canCollect, enabled, change,
                            onEdit = { singlePosition = it },
                            onStep = { position, direction -> model.updateDraft { current ->
                                val updated = SingleNumberEditor.step(current, position, direction)
                                if (updated.singleEdit.value != current.singleEdit.value) {
                                    changeSequence++
                                    change = DigitChange(position, current.singleEdit.value[position], updated.singleEdit.value[position], changeSequence)
                                }
                                updated
                            } },
                            onCollect = { model.updateDraft(SingleNumberEditor::collect) })
                        SmallLabel(when {
                            draft.singleEdit.original != null -> stringResource(R.string.studio_replacing, draft.singleEdit.original.orEmpty())
                            canCollect || (draft.singleEdit.value.isNotEmpty() && draft.singleEdit.value.length < 3) -> stringResource(R.string.studio_pending)
                            else -> stringResource(R.string.studio_tap_or_slide)
                        }, Modifier.padding(vertical = 6.dp))
                    }
                    PickMethod.GROUP, PickMethod.DISTINCT -> {
                        if (input.method == PickMethod.GROUP && input.byNumber) {
                            Surface(onClick = { batch = true }, enabled = enabled, color = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer, shape = RoundedCornerShape(6.dp)) {
                                Row(Modifier.fillMaxWidth().height(84.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(stringResource(R.string.studio_by_number), fontSize = 11.sp)
                                        Text(stringResource(R.string.studio_collected_count, input.manual.size), fontSize = 18.sp)
                                    }
                                    Text(stringResource(R.string.studio_batch)); Icon(Icons.Outlined.Edit, null, Modifier.padding(start = 8.dp).size(16.dp))
                                }
                            }
                        } else DigitSetRail(input.digits, enabled) { digitPosition = -1 }
                        SmallLabel(stringResource(when {
                            input.method == PickMethod.DISTINCT -> R.string.studio_distinct_hint
                            input.play == PlayType.GROUP3 -> R.string.studio_group3_hint
                            else -> R.string.studio_group6_hint
                        }), Modifier.padding(vertical = 6.dp))
                    }
                    PickMethod.POSITION -> {
                        PositionRail(input.positions, enabled) { digitPosition = it }
                        SmallLabel(if (input.positions.size == 3 && input.positions.all { it.isNotEmpty() })
                            stringResource(R.string.studio_position_formula, input.positions[0].size, input.positions[1].size, input.positions[2].size,
                                input.positions.fold(1) { total, digits -> total * digits.size })
                            else stringResource(R.string.studio_position_hint), Modifier.padding(vertical = 6.dp))
                    }
                    PickMethod.FILTER, PickMethod.DAN_TUO -> WorkspacePanel {
                        Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text(methodLabel(input.method), style = MaterialTheme.typography.titleMedium); SmallLabel(stringResource(R.string.studio_check_then_save)) }
                            TextButton(onClick = onTools) { Text(stringResource(R.string.studio_adjust_conditions)) }
                        }
                    }
                }
                WorkspacePanel {
                    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(start = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.studio_count, result.numbers.size, result.numbers.size * draft.multiplier),
                            Modifier.weight(1f).testTag("pick_count"), style = MaterialTheme.typography.bodySmall)
                        if (input.method == PickMethod.SINGLE) TextButton(onClick = { batch = true }, enabled = enabled,
                            modifier = Modifier.height(48.dp), contentPadding = PaddingValues(horizontal = 6.dp)) { Text(stringResource(R.string.studio_batch)) }
                        TextButton(onClick = model::newEntry, enabled = enabled, modifier = Modifier.height(48.dp).testTag("new_pick"),
                            contentPadding = PaddingValues(horizontal = 6.dp)) { Text(stringResource(if (draft.editingId == null) R.string.studio_clear else R.string.studio_new)) }
                        TextButton(onClick = onDetails, enabled = candidates.isNotEmpty(),
                            modifier = Modifier.height(48.dp).semantics { contentDescription = viewDescription }, contentPadding = PaddingValues(horizontal = 6.dp)) {
                            Text(stringResource(R.string.studio_details)); Icon(Icons.Outlined.ChevronRight, null, Modifier.size(16.dp))
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    if (candidates.isEmpty()) {
                        Box(Modifier.fillMaxWidth().heightIn(min = 96.dp).padding(16.dp), contentAlignment = Alignment.CenterStart) {
                            SmallLabel(emptyPickLabel(input, base.problem))
                        }
                    } else {
                        candidates.take(6).chunked(2).forEach { row ->
                            Row(Modifier.fillMaxWidth()) {
                                row.forEach { number ->
                                    ResultCell(number, number in result.numbers,
                                        filtered = number !in base.numbers,
                                        editing = input.method == PickMethod.SINGLE && draft.singleEdit.original == number,
                                        enabled = enabled, modifier = Modifier.weight(1f),
                                        onEdit = if (input.method == PickMethod.SINGLE) ({ model.updateDraft { SingleNumberEditor.begin(it, number) } }) else null,
                                        onToggle = { model.updateInput { it.copy(excluded = if (number in it.excluded) it.excluded - number else it.excluded + number) } })
                                }
                                if (row.size == 1) Spacer(Modifier.weight(1f))
                            }
                            HorizontalDivider(Modifier.padding(horizontal = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .6f))
                        }
                    }
                    if (result.problem == PickProblem.NO_RESULTS && base.numbers.isNotEmpty()) SmallLabel(stringResource(R.string.studio_all_excluded), Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
                Surface(onClick = onTools, enabled = enabled, color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("pick_filter"), shape = RoundedCornerShape(6.dp)) {
                    Row(Modifier.height(48.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Tune, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(stringResource(R.string.studio_filter), Modifier.padding(start = 8.dp).weight(1f))
                        SmallLabel(stringResource(if (input.conditionsJson.isBlank()) R.string.studio_filter_none else R.string.studio_filter_active))
                        Icon(Icons.Outlined.ChevronRight, null, Modifier.padding(start = 6.dp).size(16.dp))
                    }
                }
            }
        }
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.studio_amount, result.numbers.size * draft.multiplier * 2),
                        Modifier.weight(1f).testTag("pick_amount"), fontFamily = NumberFont, fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, maxLines = 1)
                    TextButton(onClick = { multiplierEditor = true }, enabled = enabled,
                        modifier = Modifier.height(48.dp).testTag("edit_multiplier"), contentPadding = PaddingValues(horizontal = 8.dp)) {
                        Text(stringResource(R.string.studio_multiplier, draft.multiplier)); Icon(Icons.Outlined.ExpandMore, null, Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = model::save, modifier = Modifier.width(104.dp).height(48.dp).testTag("save_plan"),
                        shape = RoundedCornerShape(6.dp), contentPadding = PaddingValues(horizontal = 8.dp),
                        enabled = enabled && result.problem == null && draft.issue.length == 7) {
                        if (state.saving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        else Text(stringResource(if (draft.editingId == null) R.string.studio_save else R.string.studio_update_plan))
                    }
                }
            }
        }
    }
    if (singlePosition >= 0) SingleNumberSheet(draft.singleEdit, singlePosition,
        onDismiss = { singlePosition = -1 },
        onValue = { value -> model.updateDraft { SingleNumberEditor.setValue(it, value) } },
        onRemove = { draft.singleEdit.original?.let { original -> model.updateDraft { SingleNumberEditor.remove(it, original) } }; singlePosition = -1 },
        onCancelEdit = { model.updateDraft { it.copy(singleEdit = SingleNumberEdit()) }; singlePosition = -1 })
    if (digitPosition >= -1) DigitSetSheet(
        title = if (digitPosition == -1) stringResource(R.string.studio_edit_digits) else stringResource(R.string.studio_edit_position, labels[digitPosition]),
        initial = if (digitPosition == -1) input.digits else input.positions.getOrNull(digitPosition).orEmpty(),
        onDismiss = { digitPosition = -2 },
        onApply = { digits ->
            val position = digitPosition
            model.updateInput { current ->
                if (position == -1) {
                    if (current.digits == digits) current else current.copy(digits = digits, excluded = emptySet())
                } else {
                    if (current.positions.getOrNull(position) == digits) current
                    else current.copy(positions = List(3) { index -> if (index == position) digits else current.positions.getOrNull(index).orEmpty() }, excluded = emptySet())
                }
            }
            digitPosition = -2
        })
    if (issueEditor) IssueSheet(draft.issue, { issueEditor = false }) { value -> model.updateDraft { it.copy(issue = value) }; issueEditor = false }
    if (multiplierEditor) MultiplierSheet(draft.multiplier, { multiplierEditor = false }) { value -> model.updateDraft { it.copy(multiplier = value) }; multiplierEditor = false }
    if (batch) BatchNumberSheet(input, { batch = false }) { numbers ->
        model.updateInput { it.copy(manual = (it.manual + numbers).distinct(), byNumber = it.method == PickMethod.GROUP, excluded = it.excluded - numbers.toSet()) }
        batch = false
    }
    if (help) WorkspaceSheet(stringResource(R.string.studio_help_title), { help = false }) {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(PickMethod.SINGLE to R.string.studio_help_single, PickMethod.GROUP to R.string.studio_help_group,
                PickMethod.DISTINCT to R.string.studio_help_distinct, PickMethod.POSITION to R.string.studio_help_position).forEach { (method, body) ->
                Column { Text(methodLabel(method), fontWeight = FontWeight.SemiBold); SmallLabel(stringResource(body)) }
            }
            SmallLabel(stringResource(R.string.studio_help_footer))
        }
    }
}

@Composable internal fun ResultCell(number: String, kept: Boolean, filtered: Boolean, editing: Boolean, enabled: Boolean,
    modifier: Modifier = Modifier, onEdit: (() -> Unit)?, onToggle: () -> Unit) {
    val editDescription = stringResource(R.string.studio_edit_number, number)
    val keepDescription = stringResource(R.string.studio_keep_number, number)
    Surface(modifier, color = if (editing) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface) {
        Row(Modifier.height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f).fillMaxHeight().clickable(enabled = enabled && (!filtered || onEdit != null), onClick = onEdit ?: onToggle)
                .semantics { contentDescription = if (onEdit == null) keepDescription else editDescription; selected = if (onEdit == null) kept else editing }
                .testTag("result_$number").padding(start = 16.dp), contentAlignment = Alignment.CenterStart) {
                Text(number, fontFamily = NumberFont, fontSize = 16.sp, fontWeight = FontWeight.Medium,
                    color = if (kept) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = if (kept) TextDecoration.None else TextDecoration.LineThrough)
            }
            Checkbox(checked = kept, onCheckedChange = { onToggle() }, enabled = enabled && !filtered,
                modifier = Modifier.size(48.dp).testTag("keep_$number").semantics { contentDescription = keepDescription })
        }
    }
}

@Composable private fun emptyPickLabel(input: PickInput, problem: PickProblem?): String = stringResource(when {
    problem == PickProblem.NO_RESULTS -> R.string.studio_no_results
    problem == PickProblem.INVALID_SHAPE -> R.string.studio_invalid_shape
    problem != null && problem != PickProblem.INCOMPLETE -> R.string.studio_invalid_input
    input.method == PickMethod.SINGLE || input.byNumber -> R.string.studio_enter_one
    input.method == PickMethod.POSITION -> R.string.studio_need_positions
    input.method == PickMethod.GROUP && input.play == PlayType.GROUP3 -> R.string.studio_need_two
    else -> R.string.studio_need_three
})

@Composable private fun IssueSheet(initial: String, onDismiss: () -> Unit, onApply: (String) -> Unit) {
    var value by rememberSaveable { mutableStateOf(initial) }
    WorkspaceSheet(stringResource(R.string.studio_choose_issue), onDismiss) {
        OutlinedTextField(value, { if (it.length <= 7 && it.all { char -> char in '0'..'9' }) value = it },
            label = { Text(stringResource(R.string.studio_issue_label)) }, placeholder = { Text(stringResource(R.string.studio_issue_placeholder)) },
            singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().testTag("issue_input"))
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            SmallLabel(stringResource(R.string.studio_issue_hint), Modifier.weight(1f))
            Button(onClick = { onApply(value) }, enabled = value.length == 7, modifier = Modifier.height(48.dp).testTag("apply_issue")) { Text(stringResource(R.string.studio_apply_issue)) }
        }
    }
}

@Composable private fun MultiplierSheet(initial: Int, onDismiss: () -> Unit, onApply: (Int) -> Unit) {
    var value by rememberSaveable { mutableStateOf(initial.toString()) }
    val number = value.toIntOrNull()
    val valid = number != null && number in 1..99
    WorkspaceSheet(stringResource(R.string.studio_multiplier_title), onDismiss) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            IconButton(onClick = { value = ((number ?: 1) - 1).coerceIn(1, 99).toString() }, enabled = number != null && number > 1) { Icon(Icons.Outlined.Remove, stringResource(R.string.studio_less)) }
            OutlinedTextField(value, { if (it.length <= 2 && it.all { char -> char in '0'..'9' }) value = it },
                modifier = Modifier.width(104.dp).testTag("multiplier_input"), singleLine = true, isError = !valid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            IconButton(onClick = { value = ((number ?: 0) + 1).coerceIn(1, 99).toString() }, enabled = number == null || number < 99) { Icon(Icons.Outlined.Add, stringResource(R.string.studio_more)) }
        }
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            SmallLabel(stringResource(if (valid) R.string.studio_multiplier_hint else R.string.studio_multiplier_error), Modifier.weight(1f))
            Button(onClick = { number?.takeIf { it in 1..99 }?.let(onApply) }, enabled = valid, modifier = Modifier.height(48.dp).testTag("apply_multiplier")) { Text(stringResource(R.string.studio_apply_multiplier)) }
        }
    }
}

@Composable private fun BatchNumberSheet(input: PickInput, onDismiss: () -> Unit, onApply: (List<String>) -> Unit) {
    var value by rememberSaveable { mutableStateOf("") }
    var invalid by rememberSaveable { mutableStateOf(false) }
    WorkspaceSheet(stringResource(R.string.studio_batch_title, if (input.method == PickMethod.GROUP) playLabel(input.play) else methodLabel(PickMethod.SINGLE)), onDismiss) {
        OutlinedTextField(value, { value = it; invalid = false }, label = { Text(stringResource(R.string.studio_number_label)) },
            placeholder = { Text(stringResource(R.string.studio_batch_placeholder)) },
            supportingText = { Text(stringResource(if (invalid) R.string.studio_batch_invalid else R.string.studio_batch_hint)) },
            minLines = 3, isError = invalid, modifier = Modifier.fillMaxWidth().testTag("batch_input"))
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss, modifier = Modifier.height(48.dp)) { Text(stringResource(R.string.studio_cancel)) }
            Button(onClick = {
                val numbers = value.trim().split(Regex("[\\s,，;；]+"))
                val added = PickEngine.generate(PickInput(method = input.method, play = input.play, manual = numbers, byNumber = input.method == PickMethod.GROUP))
                if (added.problem == null) onApply(added.numbers) else invalid = true
            }, modifier = Modifier.height(48.dp)) { Text(stringResource(R.string.studio_add_numbers)) }
        }
    }
}
