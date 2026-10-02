package com.sanxu.data.repository
import com.sanxu.domain.repository.*
import com.sanxu.domain.filter.ConditionCodec

import com.google.common.truth.Truth.assertThat
import com.sanxu.domain.attributes.GroupShape
import com.sanxu.domain.filter.BigCountAllowed
import com.sanxu.domain.filter.ConsecutiveCondition
import com.sanxu.domain.filter.DanTuoCondition
import com.sanxu.domain.filter.GlobalExcludedDigits
import com.sanxu.domain.filter.GlobalRequiredDigits
import com.sanxu.domain.filter.GroupShapeCondition
import com.sanxu.domain.filter.OddCountAllowed
import com.sanxu.domain.filter.PairMetric
import com.sanxu.domain.filter.PairPosition
import com.sanxu.domain.filter.PairRelationRange
import com.sanxu.domain.filter.Position
import com.sanxu.domain.filter.PositionAllowed
import com.sanxu.domain.filter.PrimeLikeCountAllowed
import com.sanxu.domain.filter.PlayType
import com.sanxu.domain.filter.RouteAllowed
import com.sanxu.domain.filter.SpanRange
import com.sanxu.domain.filter.SumRange
import com.sanxu.domain.filter.SumTailAllowed
import org.junit.Test

class ConditionCodecTest {
    @Test
    fun `all V1 condition types round trip without losing parameters`() {
        val conditions = listOf(
            GlobalRequiredDigits(setOf(0, 7)),
            GlobalExcludedDigits(setOf(9)),
            PositionAllowed(Position.HUNDREDS, setOf(0, 1, 2)),
            SumRange(6, 15),
            SumTailAllowed(setOf(0, 5)),
            SpanRange(2, 8),
            OddCountAllowed(setOf(1, 3)),
            BigCountAllowed(setOf(0, 2)),
            PrimeLikeCountAllowed(setOf(1, 2)),
            RouteAllowed(Position.TENS, setOf(0, 2)),
            GroupShapeCondition(setOf(GroupShape.GROUP3, GroupShape.GROUP6)),
            ConsecutiveCondition(requirePair = true, requireTriple = false),
            PairRelationRange(
                position = PairPosition.HUNDREDS_ONES,
                metric = PairMetric.ABSOLUTE_DIFFERENCE,
                minimum = 2,
                maximum = 7,
            ),
            DanTuoCondition(setOf(0), setOf(1, 2, 3), PlayType.GROUP6),
        )

        val encoded = ConditionCodec.encode(conditions)

        assertThat(ConditionCodec.decode(encoded)).containsExactlyElementsIn(conditions).inOrder()
        assertThat(encoded).contains("\"schemaVersion\":1")
        assertThat(encoded).contains("\"ruleVersion\":1")
    }
}
