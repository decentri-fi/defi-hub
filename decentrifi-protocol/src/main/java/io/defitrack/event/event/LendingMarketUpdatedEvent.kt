package io.defitrack.event.event

import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.networkinfo.NetworkInformation
import io.defitrack.networkinfo.toNetworkInformation
import io.defitrack.market.domain.lending.LendingMarket
import java.math.BigDecimal

data class LendingMarketUpdatedEvent(
    val id: String,
    val price: BigDecimal,
    val marketToken: FungibleTokenInformation?,
    val network: NetworkInformation
) : MarketUpdatedEvent("lending")

fun LendingMarket.createLendingMarketUpdatedEvent(): LendingMarketUpdatedEvent = LendingMarketUpdatedEvent(
    id = id,
    price = price.get(),
    marketToken = marketToken,
    network = network.toNetworkInformation()
)