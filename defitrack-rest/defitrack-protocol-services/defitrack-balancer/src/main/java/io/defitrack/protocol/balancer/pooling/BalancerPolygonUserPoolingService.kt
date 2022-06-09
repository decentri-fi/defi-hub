package io.defitrack.protocol.balancer.pooling

import io.defitrack.common.network.Network
import io.defitrack.pool.UserPoolingService
import io.defitrack.pool.domain.PoolingElement
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.polygon.BalancerPolygonPoolGraphProvider
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class BalancerPolygonUserPoolingService(private val balancerPolygonPoolGraphProvider: BalancerPolygonPoolGraphProvider) :
    UserPoolingService() {

    override suspend fun fetchUserPoolings(address: String): List<PoolingElement> {
        val poolShares = balancerPolygonPoolGraphProvider.getBalances(address).filter {
            it.balance > BigDecimal.ZERO
        }

        return poolShares.map { share ->
            val market = PoolingMarketElement(
                id = "balancer-polygon-${share.poolId.id}",
                network = getNetwork(),
                protocol = getProtocol(),
                address = share.poolId.address,
                name = share.poolId.name,
                symbol = share.poolId.symbol,
                tokens = emptyList(),
                tokenType = TokenType.BALANCER,
                apr = null,
                marketSize = null
            )
            poolingElement(
                market = market,
                amount = share.balance,
            )
        }
    }

    override fun getProtocol(): Protocol = Protocol.BALANCER

    override fun getNetwork(): Network = Network.POLYGON
}