package io.defitrack.market.domain.lending

import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.market.domain.MarketInformation
import io.defitrack.networkinfo.NetworkInformation
import io.defitrack.protocol.ProtocolInformation
import java.math.BigDecimal

class LendingMarketInformation(
    id: String,
    name: String,
    protocol: ProtocolInformation,
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
) : MarketInformation(
    id,
    network,
    protocol,
    name,
    prepareInvestmentSupported,
    exitPositionSupported,
    marketSize,
    "lending",
    updatedAt,
    deprecated
)