package io.defitrack.event.event

import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.DefiMarket
import io.defitrack.market.domain.lending.LendingMarket

sealed class MarketUpdatedEvent(val type: String)

fun DefiMarket.marketUpdatedEvent(): MarketUpdatedEvent? {
    return when (this) {
        is PoolingMarket -> createPoolMarketUpdatedEvent()
        is LendingMarket -> createLendingMarketUpdatedEvent()
        else -> null
    }
}