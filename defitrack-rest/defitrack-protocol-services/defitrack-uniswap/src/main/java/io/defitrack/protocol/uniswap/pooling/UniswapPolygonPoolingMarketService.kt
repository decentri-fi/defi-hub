package io.defitrack.protocol.uniswap.pooling

import io.defitrack.common.network.Network
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.pool.domain.PoolingToken
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.uniswap.apr.UniswapAPRService
import io.defitrack.uniswap.AbstractUniswapService
import org.springframework.stereotype.Component

@Component
class UniswapPolygonPoolingMarketService(
    private val uniswapServices: List<AbstractUniswapService>,
    private val uniswapAPRService: UniswapAPRService,
) : PoolingMarketService() {

    override fun fetchPoolingMarkets(): List<PoolingMarketElement> {
        return uniswapServices.filter {
            it.getNetwork() == getNetwork()
        }.flatMap {
            it.getPairs().mapNotNull {
                try {
                    PoolingMarketElement(
                        network = getNetwork(),
                        protocol = getProtocol(),
                        id = "uniswap-polygon-${it.id}",
                        name = "UNI ${it.token0.symbol}-${it.token1.symbol}",
                        address = it.id,
                        token = listOf(
                            PoolingToken(
                                it.token0.name,
                                it.token0.symbol,
                                it.token0.id
                            ),
                            PoolingToken(
                                it.token1.name,
                                it.token1.symbol,
                                it.token1.id
                            ),
                        ),
                        apr = uniswapAPRService.getAPR(it.id),
                        marketSize = it.reserveUSD
                    )
                } catch (ex: Exception) {
                    logger.error("something went wrong trying to import uniswap market ${it.id}")
                    null
                }
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.UNISWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}