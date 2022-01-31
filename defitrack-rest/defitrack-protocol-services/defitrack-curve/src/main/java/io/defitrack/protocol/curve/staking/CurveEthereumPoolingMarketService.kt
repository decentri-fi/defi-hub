package io.defitrack.protocol.curve.staking

import io.defitrack.common.network.Network
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.pool.domain.PoolingToken
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.CurveEthereumService
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class CurveEthereumPoolingMarketService(
    private val curveEthereumService: CurveEthereumService
) : PoolingMarketService() {

    override suspend fun fetchPoolingMarkets(): List<PoolingMarketElement> {
        return curveEthereumService.getPools().map { pool ->
            PoolingMarketElement(
                id = "curve-ethereum-${pool.id}",
                network = getNetwork(),
                protocol = getProtocol(),
                address = pool.id,
                name = pool.name,
                token = pool.coins.map { coin ->
                    PoolingToken(
                        name = coin.underlying.token.name,
                        symbol = coin.underlying.token.symbol,
                        address = coin.underlying.token.address
                    )
                },
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