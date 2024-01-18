package io.defitrack.market

import io.defitrack.network.NetworkInformation
import io.defitrack.protocol.ProtocolInformation
import java.math.BigDecimal

abstract class MarketVO(
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