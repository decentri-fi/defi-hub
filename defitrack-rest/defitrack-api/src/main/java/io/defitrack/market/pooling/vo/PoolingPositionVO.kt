package io.defitrack.market.pooling.vo

import io.defitrack.network.NetworkInformation
import io.defitrack.protocol.ProtocolInformation
import java.math.BigDecimal
import java.math.BigInteger

class PoolingPositionVO(
    val lpAddress: String,
    val amountDecimal: BigDecimal,
    val amount: BigInteger,
    val name: String,
    val breakdown: List<PoolingPositionTokenshareVO>?,
    val network: NetworkInformation,
    val symbol: String,
    val protocol: ProtocolInformation,
    val dollarValue: Double,
    val id: String,
    val exitPositionSupported: Boolean,
    val marketType: String = "pooling",
    val market: PoolingMarketVO
)