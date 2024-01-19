package io.defitrack.market.port.`in`

import io.defitrack.market.domain.GetHistoryCommand
import io.defitrack.market.domain.PoolingMarketEvent

interface PoolingHistory {
    suspend fun getPoolingHistory(getPoolingHistoryCommand: GetHistoryCommand): List<PoolingMarketEvent>
}