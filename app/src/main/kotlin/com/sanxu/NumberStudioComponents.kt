package com.sanxu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanxu.domain.pick.NumberSetInput
import com.sanxu.domain.pick.SingleNumberEdit
import com.sanxu.ui.NumberFont
import kotlin.math.abs

internal data class DigitChange(val position: Int, val before: Char, val after: Char, val sequence: Int)

@Composable internal fun positionLabels() = listOf(
    stringResource(R.string.studio_hundreds),
    stringResource(R.string.studio_tens),
    stringResource(R.string.studio_units),
)

private fun digitSummary(digits: Set<Int>) = when {
    digits.isEmpty() -> "—"
    digits.size == 10 -> "0–9"
    else -> digits.sorted().joinToString(" ")
}

@Composable internal fun SingleNumberRail(
    edit: SingleNumberEdit,
    canCollect: Boolean,
    enabled: Boolean,
    change: DigitChange?,
    onEdit: (Int) -> Unit,
    onStep: (Int, Int) -> Unit,
    onCollect: () -> Unit,
) {
    val labels = positionLabels()
    val stepPixels = with(LocalDensity.current) { 32.dp.toPx() }
    val currentStep by rememberUpdatedState(onStep)
    Surface(color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer, shape = RoundedCornerShape(6.dp)) {
        Row(Modifier.fillMaxWidth().height(84.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(3) { position ->
                var distance by remember { mutableFloatStateOf(0f) }
                val description = stringResource(R.string.studio_edit_position, labels[position])
                Column(
                    Modifier.weight(1f).fillMaxHeight().testTag("single_digit_$position")
                        .draggable(
                            state = rememberDraggableState { delta ->
                                distance -= delta
                                while (abs(distance) >= stepPixels) {
                                    val direction = if (distance > 0f) 1 else -1
                                    currentStep(position, direction)
                                    distance -= direction * stepPixels
                                }
                            },
                            orientation = Orientation.Vertical,
                            enabled = enabled && edit.value.length == 3,
                            onDragStarted = { distance = 0f },
                            onDragStopped = { distance = 0f },
                        )
                        .clickable(enabled = enabled) { onEdit(position) }
                        .semantics { contentDescription = description },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(labels[position], fontSize = 11.sp, lineHeight = 16.sp, color = LocalContentColor.current.copy(alpha = .8f))
                    Text(edit.value.getOrNull(position)?.toString() ?: "—", fontFamily = NumberFont,
                        fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Medium)
                    val changing = change?.takeIf { it.position == position }
                    Text(changing?.let { "${it.before} → ${it.after}" } ?: " ",
                        fontFamily = NumberFont, fontSize = 11.sp, lineHeight = 14.sp,
                        color = LocalContentColor.current.copy(alpha = .8f))
                }
            }
            Box(Modifier.width(1.dp).height(40.dp).background(LocalContentColor.current.copy(alpha = .18f)))
            Button(onClick = onCollect, enabled = enabled && canCollect,
                modifier = Modifier.padding(horizontal = 8.dp).width(72.dp).height(48.dp).testTag("collect_number"),
                shape = RoundedCornerShape(6.dp), contentPadding = PaddingValues(horizontal = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = .12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = .7f),
                )) {
                Text(stringResource(when {
                    edit.original != null -> R.string.studio_update_number
                    !canCollect && edit.value.length == 3 -> R.string.studio_collected
                    else -> R.string.studio_collect
                }))
            }
        }
    }
}

@Composable internal fun DigitSetRail(digits: Set<Int>, enabled: Boolean, onEdit: () -> Unit) {
    Surface(onClick = onEdit, enabled = enabled, modifier = Modifier.fillMaxWidth().testTag("edit_digit_set"),
        color = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(6.dp)) {
        Row(Modifier.heightIn(min = 84.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.studio_digit_set), fontSize = 11.sp, color = LocalContentColor.current.copy(alpha = .8f))
                Text(if (digits.isEmpty()) stringResource(R.string.studio_set_placeholder) else digitSummary(digits),
                    fontFamily = if (digits.isEmpty()) null else NumberFont,
                    fontSize = if (digits.isEmpty()) 16.sp else if (digits.size > 6 && digits.size < 10) 18.sp else 24.sp,
                    fontWeight = FontWeight.Medium)
            }
            Text(stringResource(R.string.studio_edit_digits), modifier = Modifier.padding(start = 8.dp), fontSize = 13.sp)
            Icon(Icons.Outlined.Edit, null, Modifier.padding(start = 6.dp).size(16.dp))
        }
    }
}

@Composable internal fun PositionRail(positions: List<Set<Int>>, enabled: Boolean, onEdit: (Int) -> Unit) {
    val labels = positionLabels()
    Surface(color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer, shape = RoundedCornerShape(6.dp)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 84.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(3) { index ->
                val digits = positions.getOrNull(index).orEmpty()
                val description = stringResource(R.string.studio_edit_position, labels[index])
                Column(Modifier.weight(1f).heightIn(min = 84.dp).clickable(enabled = enabled) { onEdit(index) }
                    .testTag("position_$index").semantics { contentDescription = description }
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)) {
                    Text(labels[index], fontSize = 11.sp, color = LocalContentColor.current.copy(alpha = .8f))
                    Text(digitSummary(digits), fontFamily = NumberFont, fontSize = if (digits.size > 4 && digits.size < 10) 16.sp else 22.sp,
                        lineHeight = 24.sp, fontWeight = FontWeight.Medium, maxLines = 2)
                    Box(Modifier.width(32.dp).height(1.dp).background(LocalContentColor.current.copy(alpha = .55f)))
                }
                if (index < 2) Box(Modifier.width(1.dp).height(40.dp).background(LocalContentColor.current.copy(alpha = .18f)))
            }
        }
    }
}

@Composable internal fun DigitSetSheet(title: String, initial: Set<Int>, onDismiss: () -> Unit, onApply: (Set<Int>) -> Unit) {
    var value by rememberSaveable { mutableStateOf(initial.sorted().joinToString("")) }
    val parsed = remember(value) { NumberSetInput.parse(value) }
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val apply: () -> Unit = { parsed?.let { keyboard?.hide(); onApply(it) } }
    WorkspaceSheet(title, onDismiss) {
        OutlinedTextField(value, { value = it }, singleLine = true,
            label = { Text(stringResource(R.string.studio_digit_set)) },
            supportingText = { Text(stringResource(if (parsed == null) R.string.studio_set_invalid else R.string.studio_set_hint)) },
            isError = parsed == null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { apply() }),
            modifier = Modifier.fillMaxWidth().focusRequester(focus).testTag("digits_input"))
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { value = "0123456789" }, modifier = Modifier.height(48.dp).testTag("select_all_digits")) { Text(stringResource(R.string.studio_set_all)) }
            TextButton(onClick = { value = "" }, modifier = Modifier.height(48.dp)) { Text(stringResource(R.string.studio_set_clear)) }
            Spacer(Modifier.weight(1f))
            Button(onClick = apply, enabled = parsed != null, modifier = Modifier.height(48.dp).testTag("apply_digits")) { Text(stringResource(R.string.studio_apply_digits)) }
        }
        LaunchedEffect(Unit) { focus.requestFocus(); keyboard?.show() }
    }
}

@Composable internal fun SingleNumberSheet(
    edit: SingleNumberEdit,
    position: Int,
    onDismiss: () -> Unit,
    onValue: (String) -> Unit,
    onRemove: () -> Unit,
    onCancelEdit: () -> Unit,
) {
    val start = position.coerceAtMost(edit.value.length)
    var field by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(edit.value, TextRange(start, (start + 1).coerceAtMost(edit.value.length))))
    }
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val done: () -> Unit = { keyboard?.hide(); onDismiss() }
    WorkspaceSheet(stringResource(R.string.studio_number_title), onDismiss) {
        OutlinedTextField(field, { next ->
            if (next.text.length <= 3 && next.text.all { it in '0'..'9' }) { field = next; onValue(next.text) }
        }, label = { Text(stringResource(R.string.studio_number_label)) },
            placeholder = { Text(stringResource(R.string.studio_number_placeholder)) },
            supportingText = { Text(stringResource(R.string.studio_number_hint)) }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { done() }),
            modifier = Modifier.fillMaxWidth().focusRequester(focus).testTag("single_number_input"))
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (edit.original != null) {
                TextButton(onClick = onRemove, modifier = Modifier.height(48.dp)) { Text(stringResource(R.string.studio_remove_number), color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onCancelEdit, modifier = Modifier.height(48.dp)) { Text(stringResource(R.string.studio_cancel_edit)) }
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = done, modifier = Modifier.height(48.dp).testTag("apply_number")) { Text(stringResource(R.string.studio_apply_number)) }
        }
        LaunchedEffect(Unit) { focus.requestFocus(); keyboard?.show() }
    }
}
