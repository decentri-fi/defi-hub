package io.defitrack.market.adapter.`in`.mapper

import io.defitrack.market.adapter.`in`.resource.MarketVO
import io.defitrack.market.domain.DefiMarket

interface MarketVOMapper<T : DefiMarket> {
   suspend fun map(market: T): MarketVO
}