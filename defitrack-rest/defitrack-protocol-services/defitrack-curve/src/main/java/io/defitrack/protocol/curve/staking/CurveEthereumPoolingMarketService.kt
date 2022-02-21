package io.defitrack.protocol.curve.staking

import io.defitrack.common.network.Network
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.CurveEthereumService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class CurveEthereumPoolingMarketService(
    private val curveEthereumService: CurveEthereumService,
    private val erc20Resource: ERC20Resource
) : PoolingMarketService() {

    override suspend fun fetchPoolingMarkets(): List<PoolingMarketElement> {
        return curveEthereumService.getPools().map { pool ->

            val tokens = pool.coins.map { coin->
                erc20Resource.getTokenInformation(getNetwork(), coin.underlying.token.address)
            }

            PoolingMarketElement(
                id = "curve-ethereum-${pool.id}",
                network = getNetwork(),
                protocol = getProtocol(),
                address = pool.lpToken.address,
                name = pool.lpToken.name,
                token = tokens.map { it.toFungibleToken() },
                apr = BigDecimal.ZERO,
                marketSize = BigDecimal.ZERO
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.CURVE
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}