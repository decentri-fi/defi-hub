package io.defitrack.market.pooling.vo

import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import java.math.BigDecimal
import java.math.BigInteger

class PoolingPositionVO(
    val lpAddress: String,
    val amountDecimal: BigDecimal,
    val amount: BigInteger,
    val name: String,
    val breakdown: List<PoolingPositionTokenshareVO>?,
    val network: NetworkVO,
    val symbol: String,
    val protocol: ProtocolVO,
    val dollarValue: Double,
    val id: String,
    val exitPositionSupported: Boolean,
    val marketType: String = "pooling"
)