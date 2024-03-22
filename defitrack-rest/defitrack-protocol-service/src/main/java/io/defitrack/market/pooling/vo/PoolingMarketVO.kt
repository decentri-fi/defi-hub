package io.defitrack.market.pooling.vo

import io.defitrack.market.adapter.`in`.resource.MarketVO
import io.defitrack.networkinfo.NetworkInformation
import io.defitrack.protocol.ProtocolVO
import java.math.BigDecimal

class PoolingMarketVO(
    id: String,
    name: String,
    protocol: ProtocolVO,
    network: NetworkInformation,
    val breakdown: List<PoolingMarketTokenShareVO>,
    val apr: BigDecimal?,
    val address: String,
    val decimals: Int = 18,
    prepareInvestmentSupported: Boolean,
    exitPositionSupported: Boolean,
    val erc20Compatible: Boolean,
    val totalSupply: BigDecimal = BigDecimal.ZERO,
    val metadata: Map<String, Any>,
    updatedAt: Long,
    deprecated: Boolean
) : MarketVO(
    id,
    network,
    protocol,
    name,
    prepareInvestmentSupported,
    exitPositionSupported,
    "pooling",
    updatedAt,
    deprecated
)