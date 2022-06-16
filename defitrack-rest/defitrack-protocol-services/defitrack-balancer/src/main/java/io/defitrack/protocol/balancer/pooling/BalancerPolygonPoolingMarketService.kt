package io.defitrack.protocol.balancer.pooling

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarketElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.polygon.BalancerPolygonPoolGraphProvider
import io.defitrack.token.TokenType
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class BalancerPolygonPoolingMarketService(private val balancerPolygonPoolGraphProvider: BalancerPolygonPoolGraphProvider) :
    PoolingMarketProvider() {

    override suspend fun fetchPoolingMarkets(): List<PoolingMarketElement> {
        return balancerPolygonPoolGraphProvider.getPools().mapNotNull {
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
                    tokens = emptyList(),
                    symbol = it.symbol,
                    apr = BigDecimal.ZERO,
                    marketSize = it.totalLiquidity,
                    tokenType = TokenType.BALANCER
                )
            } else {
                null
            }
        }
    }

    override fun getProtocol(): Protocol = Protocol.BALANCER

    override fun getNetwork(): Network = Network.POLYGON
}