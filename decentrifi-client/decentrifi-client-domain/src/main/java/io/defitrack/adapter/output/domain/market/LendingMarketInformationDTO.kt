package io.defitrack.adapter.output.domain.market

import io.defitrack.adapter.output.domain.erc20.FungibleTokenInformation
import io.defitrack.adapter.output.domain.meta.ProtocolInformationDTO
import io.defitrack.adapter.output.domain.meta.NetworkInformationDTO
import java.math.BigDecimal

class LendingMarketInformationDTO(
    id: String,
    name: String,
    protocol: ProtocolInformationDTO,
    network: NetworkInformationDTO,
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
) : MarketInformationDTO(
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