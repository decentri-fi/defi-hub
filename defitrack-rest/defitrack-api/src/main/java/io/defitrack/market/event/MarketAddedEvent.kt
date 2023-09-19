package io.defitrack.market.event

import io.defitrack.market.DefiMarket
import io.defitrack.market.pooling.domain.PoolingMarket

abstract class MarketAddedEvent(val type: String) {
    companion object {
        fun create(defiMarket: DefiMarket): MarketAddedEvent? {
            return when (defiMarket) {
                is PoolingMarket -> PoolMarketAddedEvent.createPoolMarketAddedEvent( defiMarket)
                else -> null
            }
        }
    }
}