package io.defitrack.market.farming.vo

import io.defitrack.domain.FungibleToken
import io.defitrack.domain.NetworkInformation
import io.defitrack.market.MarketVO
import io.defitrack.protocol.ProtocolVO
import java.math.BigDecimal
import java.util.*

class FarmingMarketVO(
    id: String,
    network: NetworkInformation,
    protocol: ProtocolVO,
    name: String,
    val stakedToken: FungibleToken,
    val reward: List<FungibleToken>,
    marketSize: BigDecimal?,
    val apr: BigDecimal?,
    prepareInvestmentSupported: Boolean,
    exitPositionSupported: Boolean,
    val token: FungibleToken? = null,
    updatedAt: Date,
    deprecated: Boolean,
) : MarketVO(
    id,
    network,
    protocol,
    name,
    prepareInvestmentSupported,
    exitPositionSupported,
    marketSize,
    "farming",
    updatedAt.time,
    deprecated
)
