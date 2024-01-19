package io.defitrack.market.domain

import io.defitrack.event.DefiEvent

data class PoolingMarketEvent(
    val poolingmarket: PoolingMarket,
    val event: DefiEvent
)