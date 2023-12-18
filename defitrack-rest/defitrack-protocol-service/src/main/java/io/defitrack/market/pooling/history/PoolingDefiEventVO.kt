package io.defitrack.market.pooling.history

import io.defitrack.event.DefiEvent
import io.defitrack.market.pooling.vo.PoolingMarketVO

data class PoolingDefiEventVO(
    val poolingMarketVO: MinimalPoolingMarketVO,
    val defiEvent: DefiEvent
)

data class MinimalPoolingMarketVO(
    val id: String,
    val name: String
)
