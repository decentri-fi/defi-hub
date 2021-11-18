package io.defitrack.protocol.balancer.pooling

import io.codechef.defitrack.pool.UserPoolingService
import io.codechef.defitrack.pool.domain.PoolingElement
import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.BalancerPolygonService
import io.defitrack.protocol.staking.TokenType
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class BalancerPolygonUserPoolingService(private val balancerPolygonService: BalancerPolygonService) :
    UserPoolingService {

    @Cacheable(cacheNames = ["balancer-lps"], key = "'polygon-' + #address")
    override fun userPoolings(address: String): List<PoolingElement> {
        val poolShares = balancerPolygonService.getBalances(address).filter {
            it.balance > BigDecimal.ZERO
        }

        return poolShares.map { share ->
            PoolingElement(
                lpAddress = share.poolId.address,
                amount = share.balance,
                name = share.poolId.name,
                symbol = share.poolId.tokens.joinToString("/") {
                    it.symbol
                },
                network = getNetwork(),
                protocol = getProtocol(),
                tokenType = TokenType.BALANCER
            )
        }
    }

    override fun getProtocol(): Protocol = Protocol.BALANCER

    override fun getNetwork(): Network = Network.POLYGON
}