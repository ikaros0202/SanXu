package com.sanxu.feature.trend
import com.sanxu.core.model.DrawRecord
import com.sanxu.core.model.TrialNumber
import com.sanxu.domain.omission.DigitPosition
import com.sanxu.domain.omission.OmissionCalculator
import com.sanxu.domain.omission.TrendStatistics
internal fun buildTrendState(
    allDraws: List<DrawRecord>,
    window: Int,
    visiblePositions: Set<TrendPosition>,
    selectedPoint: TrendPoint? = null,
    statisticsPosition: TrendPosition = TrendPosition.HUNDREDS,
    scale: Float = 1f,
    trialNumbers: List<TrialNumber> = emptyList(),
): TrendUiState {
    val visible = allDraws.takeLast(window)
    val sums = allDraws.map { draw ->
        com.sanxu.domain.attributes.DrawAttributes.calculate(draw.number).sum
    }
    val sumOmissions = if (visible.isEmpty()) {
        emptyMap()
    } else {
        (0..27).associateWith { sum ->
            OmissionCalculator.calculateVisibleWindow(
                values = sums,
                target = sum,
                visibleWindowSize = visible.size,
                allowedRange = 0..27,
            ).omissionByDraw
        }
    }
    val visibleSums = sums.takeLast(visible.size)
    val positionOrder = TrendPosition.entries.filter(visiblePositions::contains)
    val omissionsByPosition = TrendPosition.entries.associateWith { position ->
        val fullValues = allDraws.map { it.digitAt(position) }
        if (visible.isEmpty()) {
            (0..9).associateWith { emptyList() }
        } else {
            (0..9).associateWith { digit ->
                OmissionCalculator.calculateVisibleWindow(
                    values = fullValues,
                    target = digit,
                    visibleWindowSize = visible.size,
                ).omissionByDraw
            }
        }
    }
    val points = positionOrder.flatMap { position ->
        visible.mapIndexed { rowIndex, draw ->
            val digit = draw.digitAt(position)
            TrendPoint(
                issue = draw.issue,
                rowIndex = rowIndex,
                position = position,
                digit = digit,
                omission = omissionsByPosition.getValue(position).getValue(digit)[rowIndex],
            )
        }
    }
    val tableRows = visible.mapIndexed { rowIndex, draw ->
        val attributes = com.sanxu.domain.attributes.DrawAttributes.calculate(draw.number)
        val trial = trialNumbers.firstOrNull { it.issue == draw.issue }
        TrendTableRow(
            issue = draw.issue,
            drawNumber = draw.number.value,
            omissions = TrendPosition.entries.flatMap { position ->
                (0..9).map { digit ->
                    omissionsByPosition.getValue(position).getValue(digit)[rowIndex]
                }
            },
            sumOmissions = (0..27).map { sum ->
                sumOmissions.getValue(sum)[rowIndex]
            },
            trialNumber = trial?.number,
            sum = visibleSums[rowIndex],
            sumTail = attributes.sumTail.toString(),
            span = attributes.span.toString(),
            oddEvenRatio = "${attributes.oddCount}:${attributes.evenCount}",
            bigSmallRatio = "${attributes.bigCount}:${attributes.smallCount}",
            routeRatio = attributes.routeCountPattern,
        )
    }
    val numbers = allDraws.map(DrawRecord::number)
    val statistics = positionOrder.map { position ->
        val activity = TrendStatistics.calculate(numbers, position.toDigitPosition(), window)
        val fullValues = allDraws.map { it.digitAt(position) }
        TrendPositionStatistics(
            position = position,
            requestedWindowSize = window,
            actualWindowSize = activity.actualWindowSize,
            sampleComplete = activity.sampleComplete,
            digits = activity.activities.map { digitActivity ->
                val omission = OmissionCalculator.calculate(fullValues, digitActivity.digit)
                TrendDigitStatistics(
                    digit = digitActivity.digit,
                    currentOmission = omission.currentOmission,
                    averageOmission = omission.averageOmission,
                    maxOmission = omission.maxOmission,
                    occurrences = digitActivity.occurrences,
                    heatLevel = digitActivity.level,
                )
            },
        )
    }
    val sumStatistics = (0..27).map { sum ->
        val omission = OmissionCalculator.calculate(sums, sum, 0..27)
        TrendSumStatistics(
            sum = sum,
            currentOmission = omission.currentOmission,
            averageOmission = omission.averageOmission,
            maxOmission = omission.maxOmission,
            occurrences = visibleSums.count { it == sum },
        )
    }
    return TrendUiState(
        window = window,
        visibleDraws = visible,
        visiblePositions = visiblePositions,
        tableRows = tableRows,
        points = points,
        statistics = statistics,
        sumStatistics = sumStatistics,
        selectedPoint = selectedPoint,
        statisticsPosition = statisticsPosition,
        scale = scale,
        trialNumbers = trialNumbers,
    )
}

private fun DrawRecord.digitAt(position: TrendPosition): Int = when (position) {
    TrendPosition.HUNDREDS -> number.hundreds
    TrendPosition.TENS -> number.tens
    TrendPosition.ONES -> number.ones
}

private fun TrendPosition.toDigitPosition(): DigitPosition = when (this) {
    TrendPosition.HUNDREDS -> DigitPosition.HUNDREDS
    TrendPosition.TENS -> DigitPosition.TENS
    TrendPosition.ONES -> DigitPosition.ONES
}
