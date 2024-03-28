package io.defitrack.adapter.output.domain.market

import io.defitrack.adapter.output.domain.erc20.FungibleTokenInformation
import io.defitrack.adapter.output.domain.meta.NetworkInformationDTO
import io.defitrack.adapter.output.domain.meta.ProtocolInformationDTO
import java.math.BigDecimal

class PoolingMarketInformationDTO(
    id: String,
    name: String,
    protocol: ProtocolInformationDTO,
    network: NetworkInformationDTO,
    val tokens: List<FungibleTokenInformation>,
    val breakdown: List<PoolingMarketTokenShareInformationDTO>?,
    val apr: BigDecimal?,
    val address: String,
    val decimals: Int = 18,
    marketSize: BigDecimal?,
    prepareInvestmentSupported: Boolean,
    exitPositionSupported: Boolean,
    val erc20Compatible: Boolean,
    val price: BigDecimal? = BigDecimal.ZERO,
    val totalSupply: BigDecimal = BigDecimal.ZERO,
    val metadata: Map<String, Any>,
    updatedAt: Long,
    deprecated: Boolean,
    val historySupported: Boolean = false,
) : MarketInformationDTO(
    id,
    network,
    protocol,
    name,
    prepareInvestmentSupported,
    exitPositionSupported,
    marketSize,
    "pooling",
    updatedAt,
    deprecated
)