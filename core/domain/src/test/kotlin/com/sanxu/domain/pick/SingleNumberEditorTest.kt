package com.sanxu.domain.pick

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test

/** Written for the direct editor. Execution is deferred at the user's request. */
class SingleNumberEditorTest {
    @Test fun editingNeverAddsAnUncollectedNumberToThePool() {
        val draft = SingleNumberEditor.setValue(PickDraft(), "007")
        assertThat(PickEngine.generate(draft.input).problem).isEqualTo(PickProblem.INCOMPLETE)
        assertThat(SingleNumberEditor.collect(draft).input.manual).containsExactly("007")
    }

    @Test fun anyPositionCanChangeWithoutWrappingOrLosingLeadingZeroes() {
        val draft = SingleNumberEditor.setValue(PickDraft(), "007")
        assertThat(SingleNumberEditor.step(draft, 2, 1).singleEdit.value).isEqualTo("008")
        assertThat(SingleNumberEditor.step(draft, 0, -1).singleEdit.value).isEqualTo("007")
        val last = SingleNumberEditor.setValue(draft, "999")
        assertThat(SingleNumberEditor.step(last, 1, 1)).isEqualTo(last)
    }

    @Test fun reEditingReplacesOnlyTheSelectedNumberAndKeepsOtherExclusions() {
        val initial = PickDraft(input = PickInput(manual = listOf("007", "012", "509"), excluded = setOf("012", "509")))
        val changed = SingleNumberEditor.setValue(SingleNumberEditor.begin(initial, "007"), "008")
        assertThat(changed.input.manual).containsExactly("007", "012", "509")
        val saved = SingleNumberEditor.collect(changed)
        assertThat(saved.input.manual).containsExactly("008", "012", "509").inOrder()
        assertThat(saved.input.excluded).containsExactly("012", "509")
        assertThat(saved.singleEdit.original).isNull()
    }

    @Test fun replacementCanMergeIntoAnExistingNumberAndRestoreIt() {
        val initial = PickDraft(input = PickInput(manual = listOf("007", "012"), excluded = setOf("012")))
        val edit = SingleNumberEditor.setValue(SingleNumberEditor.begin(initial, "007"), "012")
        val saved = SingleNumberEditor.collect(edit)
        assertThat(saved.input.manual).containsExactly("012")
        assertThat(saved.input.excluded).isEmpty()
        assertThat(PickEngine.generate(saved.input).numbers).containsExactly("012")
    }

    @Test fun incompleteOrInvalidInputCannotBeCollectedOrDragged() {
        val draft = SingleNumberEditor.setValue(PickDraft(), "00")
        assertThat(SingleNumberEditor.canCollect(draft)).isFalse()
        assertThat(SingleNumberEditor.collect(draft)).isEqualTo(draft)
        assertThat(SingleNumberEditor.step(draft, 1, 1)).isEqualTo(draft)
        assertThat(SingleNumberEditor.setValue(draft, "１２３")).isEqualTo(draft)
        assertThat(SingleNumberEditor.setValue(draft, "0007")).isEqualTo(draft)
    }

    @Test fun removingTheEditedNumberClearsItsEditAndExclusion() {
        val initial = PickDraft(input = PickInput(manual = listOf("007", "012"), excluded = setOf("007")))
        val removed = SingleNumberEditor.remove(SingleNumberEditor.begin(initial, "007"), "007")
        assertThat(removed.input.manual).containsExactly("012")
        assertThat(removed.input.excluded).isEmpty()
        assertThat(removed.singleEdit).isEqualTo(SingleNumberEdit())
    }

    @Test fun oldDraftJsonStillLoadsAndPendingEditRoundTrips() {
        val old = Json.decodeFromString<PickDraft>("""{"input":{"manual":["007"]},"issue":"2026300"}""")
        assertThat(old.singleEdit).isEqualTo(SingleNumberEdit())
        val pending = SingleNumberEditor.setValue(SingleNumberEditor.begin(old, "007"), "00")
        assertThat(Json.decodeFromString<PickDraft>(Json.encodeToString(pending))).isEqualTo(pending)
    }

    @Test fun digitSetInputAcceptsSeparatorsButNeverSilentlyDropsInvalidCharacters() {
        assertThat(NumberSetInput.parse("0 1, 2，0\n7")).containsExactly(0, 1, 2, 7)
        assertThat(NumberSetInput.parse("012")).containsExactly(0, 1, 2)
        assertThat(NumberSetInput.parse("")).isEmpty()
        assertThat(NumberSetInput.parse("0a1")).isNull()
        assertThat(NumberSetInput.parse("０１２")).isNull()
    }
}
