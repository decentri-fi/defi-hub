package io.defitrack.market.adapter.`in`.resource

import io.defitrack.network.NetworkInformationVO
import io.defitrack.protocol.ProtocolVO

abstract class MarketVO(
    val id: String,
    val network: NetworkInformationVO,
    val protocol: ProtocolVO,
    val name: String,
    val prepareInvestmentSupported: Boolean,
    val exitPositionSupported: Boolean,
    val marketType: String,
    val updatedAt: Long,
    val deprecated: Boolean
)