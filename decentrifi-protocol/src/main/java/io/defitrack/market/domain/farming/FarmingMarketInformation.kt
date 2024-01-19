package io.defitrack.market.domain.farming

import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.market.domain.MarketInformation
import io.defitrack.networkinfo.NetworkInformation
import io.defitrack.protocol.ProtocolInformation
import java.math.BigDecimal
import java.util.*

class FarmingMarketInformation(
    id: String,
    network: NetworkInformation,
    protocol: ProtocolInformation,
    name: String,
    val stakedToken: FungibleTokenInformation,
    val reward: List<FungibleTokenInformation>,
    marketSize: BigDecimal?,
    val apr: BigDecimal?,
    prepareInvestmentSupported: Boolean,
    exitPositionSupported: Boolean,
    val token: FungibleTokenInformation? = null,
    updatedAt: Date,
    deprecated: Boolean,
) : MarketInformation(
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