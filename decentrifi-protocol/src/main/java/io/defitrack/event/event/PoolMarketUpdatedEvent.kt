package io.defitrack.event.event

import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.networkinfo.NetworkInformation
import io.defitrack.networkinfo.toNetworkInformation
import java.math.BigDecimal
import java.math.BigInteger

data class PoolMarketUpdatedEvent(
    val breakdown: List<PoolingMarketTokenShare>,
    val id: String,
    val address: String,
    val totalSupply: BigDecimal,
    val network: NetworkInformation
) : MarketUpdatedEvent("pooling")

fun PoolingMarket.createPoolMarketUpdatedEvent(): PoolMarketUpdatedEvent = PoolMarketUpdatedEvent(
    id = id,
    address = address,
    network = network.toNetworkInformation(),
    breakdown = breakdown?.get() ?: emptyList(),
    totalSupply = totalSupply.get()
)