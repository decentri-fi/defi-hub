package io.defitrack.market.domain

import io.defitrack.protocol.ProtocolInformation
import io.defitrack.networkinfo.NetworkInformation
import java.math.BigDecimal

abstract class MarketInformation(
    val id: String,
    val network: NetworkInformation,
    val protocol: ProtocolInformation,
    val name: String,
    val prepareInvestmentSupported: Boolean,
    val exitPositionSupported: Boolean,
    val marketSize: BigDecimal?,
    val marketType: String,
    val updatedAt: Long,
    val deprecated: Boolean
)