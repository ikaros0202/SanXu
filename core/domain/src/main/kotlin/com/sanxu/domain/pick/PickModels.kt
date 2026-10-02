package com.sanxu.domain.pick

import com.sanxu.domain.filter.PlayType
import com.sanxu.domain.filter.PlayConverter
import com.sanxu.domain.filter.NumberPool
import com.sanxu.domain.filter.ConditionCodec
import com.sanxu.domain.attributes.DrawNumber
import kotlinx.serialization.Serializable

@Serializable
enum class PickMethod { SINGLE, GROUP, DISTINCT, POSITION, FILTER, DAN_TUO }

/** The original user's inputs, independent of the expanded canonical numbers. */
@Serializable
data class PickInput(
    val method: PickMethod = PickMethod.SINGLE,
    val play: PlayType = PlayType.STRAIGHT,
    val manual: List<String> = emptyList(),
    val digits: Set<Int> = emptySet(),
    val positions: List<Set<Int>> = List(3) { emptySet() },
    val byNumber: Boolean = false,
    val dan: Set<Int> = emptySet(),
    val tuo: Set<Int> = emptySet(),
    val conditionsJson: String = "",
    val excluded: Set<String> = emptySet(),
) {
    val effectivePlay: PlayType get() = when (method) {
        PickMethod.SINGLE, PickMethod.DISTINCT, PickMethod.POSITION -> PlayType.STRAIGHT
        else -> play
    }
}

enum class PickProblem { INCOMPLETE, INVALID_NUMBER, INVALID_SHAPE, INVALID_DIGIT, INVALID_CONDITIONS, NO_RESULTS }
data class PickResult(val numbers: List<String> = emptyList(), val problem: PickProblem? = null)

@Serializable
data class PickDraft(
    val input: PickInput = PickInput(),
    val issue: String = "",
    val multiplier: Int = 1,
    val editingId: String? = null,
    val revision: Int? = null,
    val note: String = "",
)

@Serializable
data class PlanEntry(
    val id: String,
    val issue: String,
    val input: PickInput,
    val numbers: List<String>,
    val multiplier: Int,
    val note: String = "",
    val createdAt: Long = 0,
    val revision: Int = 0,
) {
    val play: PlayType get() = input.effectivePlay
}

data class MergedBet(val play: PlayType, val number: String, val multiplier: Long, val sourceIds: Set<String>)
data class PlanSummary(val bets: List<MergedBet>) {
    val numberCount: Int get() = bets.size
    val unitCount: Long get() = bets.fold(0L) { total, bet -> Math.addExact(total, bet.multiplier) }
    val amountYuan: Long get() = Math.multiplyExact(unitCount, 2L)
}

object PickEngine {
    fun generate(input: PickInput): PickResult {
        val selected = input.digits + input.positions.flatten() + input.dan + input.tuo
        if (selected.any { it !in 0..9 }) return PickResult(problem = PickProblem.INVALID_DIGIT)
        val play = input.effectivePlay
        val candidates = when (input.method) {
            PickMethod.SINGLE -> return manual(input).applyFilters(input)
            PickMethod.GROUP -> {
                if (play == PlayType.STRAIGHT) return PickResult(problem = PickProblem.INVALID_SHAPE)
                if (input.byNumber) return manual(input).applyFilters(input)
                val minimum = if (play == PlayType.GROUP3) 2 else 3
                if (input.digits.size < minimum) return PickResult(problem = PickProblem.INCOMPLETE)
                PlayConverter.universe(play).filter { number -> number.digits.all(input.digits::contains) }
            }
            PickMethod.DISTINCT -> {
                if (input.digits.size < 3) return PickResult(problem = PickProblem.INCOMPLETE)
                PlayConverter.universe(PlayType.STRAIGHT).filter { it.digits.distinct().size == 3 && it.digits.all(input.digits::contains) }
            }
            PickMethod.POSITION -> {
                if (input.positions.size != 3 || input.positions.any(Set<Int>::isEmpty)) return PickResult(problem = PickProblem.INCOMPLETE)
                PlayConverter.universe(PlayType.STRAIGHT).filter { number -> number.digits.indices.all { number.digits[it] in input.positions[it] } }
            }
            PickMethod.FILTER -> PlayConverter.universe(play)
            PickMethod.DAN_TUO -> {
                if (input.dan.isEmpty() || input.tuo.isEmpty()) return PickResult(problem = PickProblem.INCOMPLETE)
                val result = NumberPool.generateDanTuo(input.dan, input.tuo, play)
                if (result.conflict != null) return PickResult(problem = PickProblem.INVALID_CONDITIONS)
                result.candidates
            }
        }
        return PickResult(numbers = candidates.map(DrawNumber::value)).applyFilters(input)
    }

    private fun manual(input: PickInput): PickResult {
        if (input.manual.isEmpty()) return PickResult(problem = PickProblem.INCOMPLETE)
        if (input.manual.any { !it.matches(Regex("[0-9]{3}")) }) return PickResult(problem = PickProblem.INVALID_NUMBER)
        return try {
            PickResult(numbers = input.manual.map { PlayConverter.canonical(DrawNumber.parse(it), input.effectivePlay).value }.distinct().sorted())
        } catch (_: IllegalArgumentException) { PickResult(problem = PickProblem.INVALID_SHAPE) }
    }

    private fun PickResult.applyFilters(input: PickInput): PickResult {
        if (problem != null) return this
        val conditions = try {
            if (input.conditionsJson.isBlank()) emptyList() else ConditionCodec.decode(input.conditionsJson)
        } catch (_: IllegalArgumentException) { return PickResult(problem = PickProblem.INVALID_CONDITIONS) }
        val filtered = numbers.filter { value -> value !in input.excluded && conditions.all { it.matches(DrawNumber.parse(value)) } }.distinct().sorted()
        return PickResult(filtered, if (filtered.isEmpty()) PickProblem.NO_RESULTS else null)
    }

    fun merge(entries: List<PlanEntry>): PlanSummary {
        require(entries.map { it.issue }.distinct().size <= 1) { "A summary belongs to one issue" }
        val bets = linkedMapOf<Pair<PlayType,String>,MergedBet>()
        entries.distinctBy(PlanEntry::id).forEach { entry ->
            require(entry.multiplier in 1..99)
            entry.numbers.map { PlayConverter.canonical(DrawNumber.parse(it),entry.play).value }.distinct().forEach { number ->
                val key = entry.play to number
                val previous = bets[key]
                bets[key] = MergedBet(entry.play,number,Math.addExact(previous?.multiplier ?: 0, entry.multiplier.toLong()),previous?.sourceIds.orEmpty() + entry.id)
            }
        }
        return PlanSummary(bets.values.sortedWith(compareBy({ it.play.ordinal },{ it.number })))
    }

    fun matches(bet: MergedBet, draw: String): Boolean = try {
        draw.matches(Regex("[0-9]{3}")) && PlayConverter.canonical(DrawNumber.parse(draw), bet.play).value == bet.number
    } catch (_: IllegalArgumentException) { false }
}
