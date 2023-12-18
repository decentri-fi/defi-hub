package io.defitrack.market.pooling.history

import io.defitrack.event.DefiEvent

data class PoolingDefiEventVO(
    val market: MinimalPoolingMarketVO,
    val event: DefiEvent
)

data class MinimalPoolingMarketVO(
    val id: String,
    val name: String
)
