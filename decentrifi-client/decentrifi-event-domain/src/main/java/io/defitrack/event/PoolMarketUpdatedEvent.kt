package io.defitrack.event

import io.defitrack.adapter.output.domain.market.PoolingMarketTokenShareInformationDTO
import io.defitrack.adapter.output.domain.meta.NetworkInformationDTO
import java.math.BigDecimal

data class PoolMarketUpdatedEvent(
    val breakdown: List<PoolingMarketTokenShareInformationDTO>?,
    val protocol: String,
    val id: String,
    val address: String,
    val totalSupply: BigDecimal,
    val network: NetworkInformationDTO,
    val erc20Compatible: Boolean?,
    val name: String?,
) : MarketUpdatedEvent("pooling")