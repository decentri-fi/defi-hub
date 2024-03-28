package io.defitrack.adapter.output.domain.market

import io.defitrack.adapter.output.domain.erc20.FungibleTokenInformation
import io.defitrack.adapter.output.domain.meta.NetworkInformationDTO
import io.defitrack.adapter.output.domain.meta.ProtocolInformationDTO
import java.math.BigDecimal
import java.util.*

class FarmingMarketInformationDTO(
    id: String,
    network: NetworkInformationDTO,
    protocol: ProtocolInformationDTO,
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
) : MarketInformationDTO(
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