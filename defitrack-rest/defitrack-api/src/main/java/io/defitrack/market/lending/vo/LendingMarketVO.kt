package io.defitrack.market.lending.vo

import io.defitrack.token.FungibleToken
import io.defitrack.market.MarketVO
import io.defitrack.network.NetworkInformation
import io.defitrack.protocol.ProtocolInformation
import java.math.BigDecimal

class LendingMarketVO(
    id: String,
    name: String,
    protocol: ProtocolInformation,
    network: NetworkInformation,
    val token: FungibleToken,
    val marketToken: FungibleToken?,
    val rate: Double?,
    val poolType: String,
    marketSize: BigDecimal?,
    prepareInvestmentSupported: Boolean,
    exitPositionSupported: Boolean,
    val erc20Compatible: Boolean,
    val price: BigDecimal,
    val totalSupply: BigDecimal,
    updatedAt: Long,
    deprecated: Boolean
) : MarketVO(
    id, network, protocol, name, prepareInvestmentSupported, exitPositionSupported, marketSize, "lending", updatedAt, deprecated
)