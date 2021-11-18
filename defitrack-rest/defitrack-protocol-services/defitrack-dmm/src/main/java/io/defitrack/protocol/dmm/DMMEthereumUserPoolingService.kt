package io.defitrack.protocol.dmm

import io.codechef.defitrack.pool.UserPoolingService
import io.codechef.defitrack.pool.domain.PoolingElement
import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.staking.TokenType
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class DMMEthereumUserPoolingService(
    private val dmmEthereumService: DMMEthereumService
) : UserPoolingService {

    @Cacheable(cacheNames = ["dmm-lps"], key = "'ethereum-' + #address")
    override fun userPoolings(address: String): List<PoolingElement> {
        return dmmEthereumService.getUserPoolings(address).flatMap {
            it.liquidityPositions
        }.map {
            PoolingElement(
                it.pool.id,
                it.liquidityTokenBalance,
                it.pool.token0.symbol + " / " + it.pool.token1.symbol + " LP",
                it.pool.token0.symbol + "-" + it.pool.token1.symbol,
                getNetwork(),
                getProtocol(),
                tokenType = TokenType.DMM
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.DMM
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}