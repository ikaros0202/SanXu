package com.sanxu.domain.pick

import kotlinx.serialization.Serializable

/** Pending text is durable, but does not enter the number pool until collected. */
@Serializable
data class SingleNumberEdit(val value: String = "", val original: String? = null)

object SingleNumberEditor {
    fun begin(draft: PickDraft, number: String): PickDraft =
        if (draft.input.method == PickMethod.SINGLE && number in draft.input.manual)
            draft.copy(singleEdit = SingleNumberEdit(number, number)) else draft

    fun setValue(draft: PickDraft, value: String): PickDraft =
        if (draft.input.method == PickMethod.SINGLE && value.length <= 3 && value.all { it in '0'..'9' })
            draft.copy(singleEdit = draft.singleEdit.copy(value = value)) else draft

    fun step(draft: PickDraft, position: Int, direction: Int): PickDraft {
        val value = draft.singleEdit.value
        if (!complete(value) || position !in 0..2 || direction !in -1..1) return draft
        val digit = (value[position].digitToInt() + direction).coerceIn(0, 9)
        return setValue(draft, value.replaceRange(position, position + 1, digit.toString()))
    }

    fun canCollect(draft: PickDraft): Boolean {
        val edit = draft.singleEdit
        if (draft.input.method != PickMethod.SINGLE || !complete(edit.value)) return false
        if (edit.original != null && edit.original !in draft.input.manual) return false
        return edit.value !in draft.input.manual || edit.value in draft.input.excluded ||
            (edit.original != null && edit.original != edit.value)
    }

    fun collect(draft: PickDraft): PickDraft {
        if (!canCollect(draft)) return draft
        val edit = draft.singleEdit
        val numbers = if (edit.original == null) draft.input.manual + edit.value
            else draft.input.manual.map { if (it == edit.original) edit.value else it }
        return draft.copy(
            input = draft.input.copy(
                manual = numbers.distinct(),
                excluded = draft.input.excluded - setOfNotNull(edit.original, edit.value),
            ),
            singleEdit = SingleNumberEdit(edit.value),
        )
    }

    fun remove(draft: PickDraft, number: String): PickDraft = draft.copy(
        input = draft.input.copy(manual = draft.input.manual - number, excluded = draft.input.excluded - number),
        singleEdit = if (draft.singleEdit.original == number) SingleNumberEdit() else draft.singleEdit,
    )

    private fun complete(value: String) = value.length == 3 && value.all { it in '0'..'9' }
}

object NumberSetInput {
    /** null denotes an invalid paste; an empty set is an intentional clear. */
    fun parse(text: String): Set<Int>? {
        if (text.any { it !in '0'..'9' && !it.isWhitespace() && it !in ",，;；" }) return null
        return text.filter { it in '0'..'9' }.map { it.digitToInt() }.toSet()
    }
}
