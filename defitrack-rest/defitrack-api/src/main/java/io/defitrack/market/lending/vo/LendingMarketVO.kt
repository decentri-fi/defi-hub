package io.defitrack.market.lending.vo

import io.defitrack.market.MarketVO
import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import io.defitrack.token.FungibleToken
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Date

class LendingMarketVO(
    id: String,
    name: String,
    protocol: ProtocolVO,
    network: NetworkVO,
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
    updatedAt: Long
) : MarketVO(
    id, network, protocol, name, prepareInvestmentSupported, exitPositionSupported, marketSize, "lending", updatedAt
)