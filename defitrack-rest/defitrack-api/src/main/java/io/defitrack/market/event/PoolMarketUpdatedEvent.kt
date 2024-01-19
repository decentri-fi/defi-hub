package io.defitrack.market.event

import io.defitrack.domain.NetworkInformation
import io.defitrack.domain.toNetworkInformation
import io.defitrack.market.pooling.domain.PoolingMarket
import java.math.BigDecimal

data class PoolMarketUpdatedEvent(
    val id: String,
    val price: BigDecimal,
    val address: String,
    val network: NetworkInformation
) : MarketUpdatedEvent("pooling")

fun PoolingMarket.createPoolMarketUpdatedEvent(): PoolMarketUpdatedEvent = PoolMarketUpdatedEvent(
    id = id,
    price = price.get(),
    address = address,
    network = network.toNetworkInformation()
)