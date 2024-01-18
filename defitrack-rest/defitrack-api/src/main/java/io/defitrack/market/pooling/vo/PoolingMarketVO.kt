package io.defitrack.market.pooling.vo

import io.defitrack.token.FungibleToken
import io.defitrack.market.MarketVO
import io.defitrack.network.NetworkInformation
import io.defitrack.protocol.ProtocolInformation
import java.math.BigDecimal

class PoolingMarketVO(
    id: String,
    name: String,
    protocol: ProtocolInformation,
    network: NetworkInformation,
    val tokens: List<FungibleToken>,
    val breakdown: List<PoolingMarketTokenShareVO>?,
    val apr: BigDecimal?,
    val address: String,
    val decimals: Int = 18,
    marketSize: BigDecimal?,
    prepareInvestmentSupported: Boolean,
    exitPositionSupported: Boolean,
    val erc20Compatible: Boolean,
    val price: BigDecimal? = BigDecimal.ZERO,
    val totalSupply: BigDecimal = BigDecimal.ZERO,
    val metadata: Map<String, Any>,
    updatedAt: Long,
    deprecated: Boolean,
    val historySupported: Boolean = false,
) : MarketVO(
    id,
    network,
    protocol,
    name,
    prepareInvestmentSupported,
    exitPositionSupported,
    marketSize,
    "pooling",
    updatedAt,
    deprecated
)