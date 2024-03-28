package io.defitrack.event.event

import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
import java.math.BigDecimal

data class PoolMarketUpdatedEvent(
    val breakdown: List<PoolingMarketTokenShare>?,
    val protocol: String,
    val id: String,
    val address: String,
    val totalSupply: BigDecimal,
    val network: NetworkInformation,
    val erc20Compatible: Boolean?,
    val name: String?,
) : MarketUpdatedEvent("pooling")

fun PoolingMarket.createPoolMarketUpdatedEvent(): PoolMarketUpdatedEvent = PoolMarketUpdatedEvent(
    id = id,
    address = address,
    protocol = protocol.slug,
    network = network.toNetworkInformation(),
    breakdown = breakdown.get(),
    totalSupply = totalSupply.get(),
    erc20Compatible = erc20Compatible,
    name = name
)