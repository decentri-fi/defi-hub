package io.defitrack.event.event

import io.defitrack.market.domain.PoolingMarket
import io.defitrack.networkinfo.NetworkInformation
import io.defitrack.networkinfo.toNetworkInformation
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