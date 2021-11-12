package io.codechef.defitrack.pool

import io.codechef.defitrack.pool.domain.PoolingMarketElement
import io.codechef.defitrack.protocol.ProtocolService

interface PoolingMarketService : ProtocolService {

    fun getPoolingMarkets(): List<PoolingMarketElement>

}