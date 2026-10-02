package com.sanxu.core.model

import com.sanxu.domain.attributes.DrawNumber
import com.sanxu.domain.scheme.HistoricalDraw

data class DrawRecord(
    val issue: String,
    val drawDate: String,
    val number: DrawNumber,
    val officialDetailUrl: String,
    val officialFingerprint: String,
    val salesAmountYuan: Long? = null,
) {
    fun asHistoricalDraw(): HistoricalDraw = HistoricalDraw(
        issue = issue,
        number = number,
        officialFingerprint = officialFingerprint,
    )
}
