package io.defitrack.market

import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import java.math.BigDecimal

abstract class MarketVO(
    val id: String,
    val network: NetworkVO,
    val protocol: ProtocolVO,
    val name: String,
    val prepareInvestmentSupported: Boolean,
    val exitPositionSupported: Boolean,
    val marketSize: BigDecimal?,
    val marketType: String,
    val updatedAt: Long,
    val deprecated: Boolean
)