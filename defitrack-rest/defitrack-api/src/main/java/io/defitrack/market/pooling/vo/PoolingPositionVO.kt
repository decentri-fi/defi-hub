package io.defitrack.market.pooling.vo

import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO

class PoolingPositionVO(
    val lpAddress: String,
    val amount: Double,
    val name: String,
    val network: NetworkVO,
    val symbol: String,
    val protocol: ProtocolVO,
    val dollarValue: Double,
    val id: String,
    val market: PoolingMarketVO
)