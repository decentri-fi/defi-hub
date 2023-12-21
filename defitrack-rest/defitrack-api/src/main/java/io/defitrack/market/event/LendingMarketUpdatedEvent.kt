package io.defitrack.market.event

import io.defitrack.erc20.FungibleToken
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.network.NetworkVO
import io.defitrack.network.toVO
import java.math.BigDecimal

data class LendingMarketUpdatedEvent(
    val id: String,
    val price: BigDecimal,
    val marketToken: FungibleToken?,
    val network: NetworkVO
) : MarketUpdatedEvent("lending")

fun LendingMarket.createLendingMarketUpdatedEvent(): LendingMarketUpdatedEvent = LendingMarketUpdatedEvent(
    id = id,
    price = price.get(),
    marketToken = marketToken,
    network = network.toVO()
)