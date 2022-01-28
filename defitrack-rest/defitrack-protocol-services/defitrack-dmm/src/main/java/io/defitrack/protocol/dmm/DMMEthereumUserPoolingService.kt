package io.defitrack.protocol.dmm

import io.defitrack.common.network.Network
import io.defitrack.pool.UserPoolingService
import io.defitrack.pool.domain.PoolingElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.staking.TokenType
import org.springframework.stereotype.Service

@Service
class DMMEthereumUserPoolingService(
    private val dmmEthereumService: DMMEthereumService
) : UserPoolingService() {

    override suspend fun fetchUserPoolings(address: String): List<PoolingElement> {
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
                tokenType = TokenType.DMM,
                id = "dmm-ethereum-${it.pool.id}",
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