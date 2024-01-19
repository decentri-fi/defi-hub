package io.defitrack.market.adapter.`in`.resource

import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.networkinfo.NetworkInformation
import io.defitrack.protocol.ProtocolVO
import java.math.BigDecimal
import java.util.*

class FarmingMarketVO(
    id: String,
    network: NetworkInformation,
    protocol: ProtocolVO,
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
