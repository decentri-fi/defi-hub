package io.defitrack.protocol.balancer.pooling

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.graph.BalancerPolygonPoolGraphProvider
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class BalancerPolygonPoolingMarketProvider(
    balancerPolygonPoolGraphProvider: BalancerPolygonPoolGraphProvider,
) : BalancerPoolingMarketProvider(balancerPolygonPoolGraphProvider) {

    override fun getNetwork(): Network = Network.POLYGON
}