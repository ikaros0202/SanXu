package com.sanxu.domain.pick

import com.sanxu.domain.filter.PlayType
import org.junit.Assert.*
import org.junit.Test

class PickEngineTest {
    @Test fun leadingZeroAndManualDuplicates() {
        assertEquals(listOf("007"), PickEngine.generate(PickInput(manual = listOf("007", "007"))).numbers)
    }
    @Test fun distinctThreeAndFourDigitPools() {
        assertEquals(listOf("012", "021", "102", "120", "201", "210"), PickEngine.generate(PickInput(PickMethod.DISTINCT, digits = setOf(0,1,2))).numbers)
        assertEquals(24, PickEngine.generate(PickInput(PickMethod.DISTINCT, digits = setOf(0,1,2,3))).numbers.size)
    }
    @Test fun groupsAreCanonicalAndDoNotContainTriples() {
        assertEquals(listOf("001","002","011","022","112","122"), PickEngine.generate(PickInput(PickMethod.GROUP, PlayType.GROUP3, digits = setOf(0,1,2))).numbers)
        assertEquals(4, PickEngine.generate(PickInput(PickMethod.GROUP, PlayType.GROUP6, digits = setOf(0,1,2,3))).numbers.size)
        assertEquals(PickProblem.INVALID_SHAPE, PickEngine.generate(PickInput(PickMethod.GROUP, PlayType.GROUP3, manual = listOf("000"), byNumber = true)).problem)
    }
    @Test fun positionsAreOrderedCrossProduct() {
        assertEquals(listOf("023","024","123","124"), PickEngine.generate(PickInput(PickMethod.POSITION, positions = listOf(setOf(0,1), setOf(2), setOf(3,4)))).numbers)
    }
    @Test fun mergeAddsMultipliersAndRemovalRestoresPriorContribution() {
        val manual = entry("a", PickInput(manual = listOf("012")), 2)
        val distinct = entry("b", PickInput(PickMethod.DISTINCT, digits = setOf(0,1,2)), 1)
        val result = PickEngine.merge(listOf(manual, distinct))
        assertEquals(6, result.numberCount)
        assertEquals(8L, result.unitCount)
        assertEquals(16L, result.amountYuan)
        assertEquals(3L, result.bets.first { it.number == "012" }.multiplier)
        assertEquals(4L, PickEngine.merge(listOf(manual)).amountYuan)
    }
    @Test fun playsStaySeparateAndGroupPermutationsMerge() {
        val entries = listOf(entry("a", PickInput(manual = listOf("012")),1), entry("b", PickInput(PickMethod.GROUP,PlayType.GROUP6,manual=listOf("210"),byNumber=true),2), entry("c", PickInput(PickMethod.GROUP,PlayType.GROUP6,manual=listOf("012"),byNumber=true),3))
        val merged = PickEngine.merge(entries)
        assertEquals(2, merged.numberCount)
        assertEquals(5L, merged.bets.first { it.play == PlayType.GROUP6 }.multiplier)
    }
    @Test fun accumulatedMultiplierCanExceed99() {
        val input = PickInput(manual = listOf("007"))
        assertEquals(100L, PickEngine.merge(listOf(entry("a",input,99),entry("b",input,1))).bets.single().multiplier)
    }
    @Test fun fullUniversesAndIncompletePosition() {
        val digits = (0..9).toSet()
        assertEquals(720,PickEngine.generate(PickInput(PickMethod.DISTINCT,digits=digits)).numbers.size)
        assertEquals(90,PickEngine.generate(PickInput(PickMethod.GROUP,PlayType.GROUP3,digits=digits)).numbers.size)
        assertEquals(120,PickEngine.generate(PickInput(PickMethod.GROUP,PlayType.GROUP6,digits=digits)).numbers.size)
        assertEquals(1000,PickEngine.generate(PickInput(PickMethod.POSITION,positions=List(3){digits})).numbers.size)
        assertEquals(PickProblem.INCOMPLETE,PickEngine.generate(PickInput(PickMethod.POSITION)).problem)
    }
    @Test fun exactAndGroupReplayDiffer() {
        assertFalse(PickEngine.matches(MergedBet(PlayType.STRAIGHT,"012",1,emptySet()),"210"))
        assertTrue(PickEngine.matches(MergedBet(PlayType.GROUP6,"012",1,emptySet()),"210"))
        assertFalse(PickEngine.matches(MergedBet(PlayType.GROUP3,"007",1,emptySet()),"077"))
    }
    @Test fun invalidAsciiInputIsRejected() {
        assertEquals(PickProblem.INVALID_NUMBER,PickEngine.generate(PickInput(manual=listOf("１２３"))).problem)
        assertEquals(PickProblem.INVALID_DIGIT,PickEngine.generate(PickInput(PickMethod.DISTINCT,digits=setOf(0,1,10))).problem)
    }
    private fun entry(id:String,input:PickInput,multiplier:Int) = PlanEntry(id,"2026260",input,PickEngine.generate(input).numbers,multiplier)
}
