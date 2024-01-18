package io.defitrack.market.event

import io.defitrack.token.FungibleToken
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.network.NetworkInformation
import io.defitrack.network.toVO
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
    network = network.toVO()
)