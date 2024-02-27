package io.defitrack.protocol.application.uniswap.v2.prefetch

import io.defitrack.market.domain.MarketInformation
import io.defitrack.networkinfo.NetworkInformation
import io.defitrack.protocol.ProtocolInformation
import java.math.BigDecimal

class PoolingMarketInformation(
    id: String,
    name: String,
    protocol: ProtocolInformation,
    network: NetworkInformation,
    val tokens: List<FungibleTokenInformation>,
    val breakdown: List<PoolingMarketTokenShareInformation>?,
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
) : MarketInformation(
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
) {

    fun hasBreakdown(): Boolean {
        return !breakdown.isNullOrEmpty()
    }
}