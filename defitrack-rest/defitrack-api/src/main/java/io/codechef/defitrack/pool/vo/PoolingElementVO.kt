package io.codechef.defitrack.pool.vo

import io.codechef.defitrack.network.NetworkVO
import io.codechef.defitrack.protocol.ProtocolVO
import java.math.BigDecimal

class PoolingElementVO(
    val lpAddress: String,
    val amount: Double,
    val name: String,
    val network: NetworkVO,
    val symbol: String,
    val protocol: ProtocolVO,
    val dollarValue: Double
)