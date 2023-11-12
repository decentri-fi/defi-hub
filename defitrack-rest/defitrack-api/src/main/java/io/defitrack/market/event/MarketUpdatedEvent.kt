package io.defitrack.market.event

import io.defitrack.market.DefiMarket
import io.defitrack.market.pooling.domain.PoolingMarket

abstract class MarketUpdatedEvent(val type: String) {
    companion object {
        fun create(defiMarket: DefiMarket): MarketUpdatedEvent? {
            return when (defiMarket) {
                is PoolingMarket -> PoolMarketUpdatedEvent.createPoolMarketAddedEvent( defiMarket)
                else -> null
            }
        }
    }
}