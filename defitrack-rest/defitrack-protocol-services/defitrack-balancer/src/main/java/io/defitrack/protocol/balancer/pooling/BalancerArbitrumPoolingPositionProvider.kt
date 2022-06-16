package io.defitrack.protocol.balancer.pooling

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.PoolingPositionProvider
import io.defitrack.market.pooling.domain.PoolingElement
import io.defitrack.market.pooling.domain.PoolingMarketElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.BalancerArbitrumService
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class BalancerArbitrumPoolingPositionProvider(private val balancerArbitrumService: BalancerArbitrumService) :
    PoolingPositionProvider() {

    override suspend fun fetchUserPoolings(address: String): List<PoolingElement> {
        val poolShares = balancerArbitrumService.getBalances(address).filter {
            it.balance > BigDecimal.ZERO
        }
        return poolShares.map { share ->
            val market = PoolingMarketElement(
                id = "balancer-arbitrum-${share.poolId.id}",
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

    override fun getNetwork(): Network = Network.ARBITRUM
}