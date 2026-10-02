package com.sanxu.data.mapper

import com.sanxu.core.model.YunnanAnnouncement
import com.sanxu.core.model.YunnanPlayAnnouncement
import com.sanxu.core.model.YunnanPlayType
import com.sanxu.data.local.YunnanAnnouncementEntity
import com.sanxu.domain.attributes.DrawNumber
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
internal data class YunnanPlayStorage(
    val playType: String,
    val winningCount: Long,
    val prizePerBetYuan: Long,
    val payoutCount: Long? = null,
    val payoutPerBetYuan: Long? = null,
)

private val YUNNAN_JSON = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

fun YunnanAnnouncement.toEntity(): YunnanAnnouncementEntity = YunnanAnnouncementEntity(
    issue = issue,
    drawDate = drawDate,
    winningNumber = number.value,
    salesAmountYuan = salesAmountYuan,
    winningTotalYuan = winningTotalYuan,
    prizePoolBalanceFen = prizePoolBalanceFen,
    playsJson = YUNNAN_JSON.encodeToString(
        plays.map { play ->
            YunnanPlayStorage(
                playType = play.playType.name,
                winningCount = play.winningCount,
                prizePerBetYuan = play.prizePerBetYuan,
                payoutCount = play.payoutCount,
                payoutPerBetYuan = play.payoutPerBetYuan,
            )
        },
    ),
    redemptionDeadline = redemptionDeadline,
    sourceUpdatedAt = sourceUpdatedAt,
    fetchedAtEpochMillis = fetchedAtEpochMillis,
    fingerprint = fingerprint,
)

fun YunnanAnnouncementEntity.toAnnouncement(): YunnanAnnouncement {
    val storedPlays = YUNNAN_JSON.decodeFromString<List<YunnanPlayStorage>>(playsJson)
    return YunnanAnnouncement(
        issue = issue,
        drawDate = drawDate,
        number = DrawNumber.parse(winningNumber),
        salesAmountYuan = salesAmountYuan,
        winningTotalYuan = winningTotalYuan,
        prizePoolBalanceFen = prizePoolBalanceFen,
        plays = storedPlays.map { play ->
            YunnanPlayAnnouncement(
                playType = YunnanPlayType.valueOf(play.playType),
                winningCount = play.winningCount,
                prizePerBetYuan = play.prizePerBetYuan,
                payoutCount = play.payoutCount,
                payoutPerBetYuan = play.payoutPerBetYuan,
            )
        },
        redemptionDeadline = redemptionDeadline,
        sourceUpdatedAt = sourceUpdatedAt,
        fetchedAtEpochMillis = fetchedAtEpochMillis,
        fingerprint = fingerprint,
    )
}
