package io.defitrack.market.lending.vo

import io.defitrack.market.MarketVO
import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import io.defitrack.token.FungibleToken
import java.math.BigDecimal

class LendingMarketVO(
    id: String,
    name: String,
    protocol: ProtocolVO,
    network: NetworkVO,
    val token: FungibleToken,
    val rate: Double?,
    val poolType: String,
    marketSize: BigDecimal?,
    prepareInvestmentSupported: Boolean
) : MarketVO(
    id, network, protocol, name, prepareInvestmentSupported, marketSize, "lending"
)