package com.sanxu.domain.livecontent

import com.sanxu.core.model.*
import com.google.common.truth.Truth.assertThat
import java.time.*
import org.junit.Test

class TrialVisibilityTest {
    private fun trial(date:String)=TrialNumber("2026264","007",TrialSource.CAIBA_55125,"https://www.55125.cn/",LocalDate.parse(date),0)
    @Test fun yesterdayDisappearsExactlyAtBeijing1630AndFutureIsNeverShown() {
        val before=ZonedDateTime.parse("2026-10-02T16:29:59+08:00[Asia/Shanghai]")
        val after=before.plusSeconds(1)
        assertThat(TrialVisibility.visible(trial("2026-10-01"),before)).isNotNull()
        assertThat(TrialVisibility.visible(trial("2026-10-01"),after)).isNull()
        assertThat(TrialVisibility.visible(trial("2026-10-02"),after)).isNotNull()
        assertThat(TrialVisibility.visible(trial("2026-09-30"),before)).isNull()
        assertThat(TrialVisibility.visible(trial("2026-10-03"),after)).isNull()
        assertThat(TrialVisibility.visible(trial("2026-10-01"),after.withZoneSameInstant(ZoneOffset.UTC))).isNull()
    }
}
