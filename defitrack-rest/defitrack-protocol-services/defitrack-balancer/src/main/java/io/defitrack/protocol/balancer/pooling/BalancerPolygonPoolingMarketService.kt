package io.defitrack.protocol.balancer.pooling

import io.defitrack.common.network.Network
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.BalancerPolygonService
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class BalancerPolygonPoolingMarketService(private val balancerPolygonService: BalancerPolygonService) :
    PoolingMarketService() {

    override suspend fun fetchPoolingMarkets(): List<PoolingMarketElement> {
        return balancerPolygonService.getPools().mapNotNull {
            if (it.totalLiquidity > BigDecimal.valueOf(100000)) {
                PoolingMarketElement(
                    id = "balancer-polygon-${it.id}",
                    network = getNetwork(),
                    protocol = getProtocol(),
                    address = it.address,
                    name = "${
                        it.tokens.joinToString("/") {
                            it.symbol
                        }
                    } Pool",
                    token = emptyList(),
                    apr = BigDecimal.ZERO,
                    marketSize = it.totalLiquidity
                )
            } else {
                null
            }
        }
    }

    override fun getProtocol(): Protocol = Protocol.BALANCER

    override fun getNetwork(): Network = Network.POLYGON
}