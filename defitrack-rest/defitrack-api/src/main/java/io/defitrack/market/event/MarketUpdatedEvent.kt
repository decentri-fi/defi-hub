package io.defitrack.market.event

import io.defitrack.market.DefiMarket
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.pooling.domain.PoolingMarket

sealed class MarketUpdatedEvent(val type: String)

fun DefiMarket.marketUpdatedEvent(): MarketUpdatedEvent? {
    return when (this) {
        is PoolingMarket -> createPoolMarketUpdatedEvent()
        is LendingMarket -> createLendingMarketUpdatedEvent()
        else -> null
    }
}