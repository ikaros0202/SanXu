package com.sanxu.domain.livecontent

import com.sanxu.core.model.TrialNumber
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.LocalTime

object TrialVisibility {
    fun visible(candidate:TrialNumber?,now:ZonedDateTime):TrialNumber? {
        val beijing=now.withZoneSameInstant(ZoneId.of("Asia/Shanghai"))
        val today=beijing.toLocalDate()
        return candidate?.takeIf {
            it.sourceLocalDate==today || (beijing.toLocalTime()<LocalTime.of(16,30)&&it.sourceLocalDate==today.minusDays(1))
        }
    }
}
