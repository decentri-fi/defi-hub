package io.defitrack.market.event

import io.defitrack.domain.FungibleToken
import io.defitrack.domain.NetworkInformation
import io.defitrack.domain.toNetworkInformation
import io.defitrack.market.lending.domain.LendingMarket
import java.math.BigDecimal

data class LendingMarketUpdatedEvent(
    val id: String,
    val price: BigDecimal,
    val marketToken: FungibleToken?,
    val network: NetworkInformation
) : MarketUpdatedEvent("lending")

fun LendingMarket.createLendingMarketUpdatedEvent(): LendingMarketUpdatedEvent = LendingMarketUpdatedEvent(
    id = id,
    price = price.get(),
    marketToken = marketToken,
    network = network.toNetworkInformation()
)