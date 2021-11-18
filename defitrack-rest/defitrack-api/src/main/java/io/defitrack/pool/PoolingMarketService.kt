package io.defitrack.pool

import io.defitrack.pool.domain.PoolingMarketElement
import io.codechef.defitrack.protocol.ProtocolService

interface PoolingMarketService : ProtocolService {

    fun getPoolingMarkets(): List<PoolingMarketElement>

}