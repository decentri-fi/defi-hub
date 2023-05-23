package io.defitrack.market

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import org.springframework.format.annotation.DateTimeFormat
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

abstract class MarketVO(
    val id: String,
    val network: NetworkVO,
    val protocol: ProtocolVO,
    val name: String,
    val prepareInvestmentSupported: Boolean,
    val exitPositionSupported: Boolean,
    val marketSize: BigDecimal?,
    val marketType: String,
    val updatedAt: Long
)