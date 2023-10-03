package io.defitrack.market

interface MarketVOMapper<T : DefiMarket> {
    fun map(market: T): MarketVO
}