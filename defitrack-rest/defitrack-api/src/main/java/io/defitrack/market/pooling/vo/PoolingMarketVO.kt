package io.defitrack.market.pooling.vo

import io.defitrack.market.MarketVO
import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import io.defitrack.token.FungibleToken
import java.math.BigDecimal

class PoolingMarketVO(
    id: String,
    address: String,
    name: String,
    protocol: ProtocolVO,
    network: NetworkVO,
    val tokens: List<FungibleToken>,
    val breakdown: List<PoolingMarketTokenShareVO>?,
    val apr: BigDecimal?,
    marketSize: BigDecimal?,
    prepareInvestmentSupported: Boolean
) : MarketVO(
    id, network, protocol, name, prepareInvestmentSupported, marketSize, "pooling"
)