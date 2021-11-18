package io.defitrack.protocol.balancer.pooling

import io.defitrack.pool.UserPoolingService
import io.defitrack.pool.domain.PoolingElement
import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.BalancerArbitrumService
import io.defitrack.protocol.staking.TokenType
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class BalancerArbitrumUserPoolingService(private val balancerArbitrumService: BalancerArbitrumService) :
    UserPoolingService {

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