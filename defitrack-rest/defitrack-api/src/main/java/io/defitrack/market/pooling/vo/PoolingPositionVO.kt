package io.defitrack.market.pooling.vo

import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import java.math.BigDecimal

class PoolingPositionVO(
    val lpAddress: String,
    val amount: BigDecimal,
    val name: String,
    val network: NetworkVO,
    val symbol: String,
    val protocol: ProtocolVO,
    val dollarValue: Double,
    val id: String,
)