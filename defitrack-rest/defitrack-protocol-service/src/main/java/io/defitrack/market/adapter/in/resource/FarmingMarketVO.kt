package io.defitrack.market.adapter.`in`.resource

import io.defitrack.erc20.FungibleTokenInformationVO
import io.defitrack.network.NetworkInformationVO
import io.defitrack.protocol.ProtocolVO
import java.math.BigDecimal
import java.util.*

class FarmingMarketVO(
    id: String,
    network: NetworkInformationVO,
    protocol: ProtocolVO,
    name: String,
    val stakedToken: FungibleTokenInformationVO,
    val reward: List<FungibleTokenInformationVO>,
    val apr: BigDecimal?,
    prepareInvestmentSupported: Boolean,
    exitPositionSupported: Boolean,
    val token: FungibleTokenInformationVO? = null,
    updatedAt: Date,
    deprecated: Boolean,
) : MarketVO(
    id,
    network,
    protocol,
    name,
    prepareInvestmentSupported,
    exitPositionSupported,
    "farming",
    updatedAt.time,
    deprecated
)
