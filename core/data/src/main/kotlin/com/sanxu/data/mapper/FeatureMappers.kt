package com.sanxu.data.mapper

import com.sanxu.core.model.DrawRecord
import com.sanxu.data.local.DrawEntity
import com.sanxu.domain.attributes.DrawNumber

fun DrawEntity.toRecord(): DrawRecord = DrawRecord(
    issue = issue,
    drawDate = drawDate,
    number = DrawNumber.of(hundreds, tens, ones),
    officialDetailUrl = officialDetailUrl,
    officialFingerprint = officialFingerprint,
    salesAmountYuan = salesAmountYuan,
)
