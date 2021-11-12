package io.codechef.defitrack.protocol.balancer.pooling

import io.codechef.common.network.Network
import io.codechef.defitrack.pool.UserPoolingService
import io.codechef.defitrack.pool.domain.PoolingElement
import io.codechef.protocol.Protocol
import io.codechef.protocol.balancer.BalancerArbitrumService
import io.codechef.protocol.staking.TokenType
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Component
class BalancerArbitrumUserPoolingService(private val balancerArbitrumService: BalancerArbitrumService) : UserPoolingService {

    @Cacheable(cacheNames = ["balancer-lps"], key = "'arbitrum-' + #address")
    override fun userPoolings(address: String): List<PoolingElement> {
        val poolShares = balancerArbitrumService.getBalances(address).filter {
            it.balance > BigDecimal.ZERO
        }
        return poolShares.map { share ->
            PoolingElement(
                lpAddress = share.poolId.address,
                amount = share.balance,
                name = share.poolId.name,
                symbol = share.poolId.symbol,
                network = getNetwork(),
                protocol = getProtocol(),
                tokenType = TokenType.BALANCER
            )
        }
    }

    override fun getProtocol(): Protocol = Protocol.BALANCER

    override fun getNetwork(): Network = Network.ARBITRUM
}