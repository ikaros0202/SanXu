package com.sanxu.data.mapper

import com.sanxu.data.local.DrawEntity
import com.sanxu.data.remote.OfficialDraw

fun OfficialDraw.toEntity(): DrawEntity = DrawEntity(
    issue = issue,
    drawDate = drawDate.toString(),
    hundreds = number.hundreds,
    tens = number.tens,
    ones = number.ones,
    officialDetailUrl = detailUrl,
    officialFingerprint = fingerprint,
    salesAmountYuan = salesAmountYuan,
)
