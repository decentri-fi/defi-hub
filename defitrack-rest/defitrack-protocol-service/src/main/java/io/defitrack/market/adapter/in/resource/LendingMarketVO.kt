package io.defitrack.market.adapter.`in`.resource

import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.networkinfo.NetworkInformation
import io.defitrack.protocol.ProtocolVO
import java.math.BigDecimal

class LendingMarketVO(
    id: String,
    name: String,
    protocol: ProtocolVO,
    network: NetworkInformation,
    val token: FungibleTokenInformation,
    val marketToken: FungibleTokenInformation?,
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
    id,
    network,
    protocol,
    name,
    prepareInvestmentSupported,
    exitPositionSupported,
    "lending",
    updatedAt,
    deprecated
)