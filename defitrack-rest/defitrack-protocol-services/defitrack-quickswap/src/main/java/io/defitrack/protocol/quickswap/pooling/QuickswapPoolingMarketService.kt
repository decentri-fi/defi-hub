package io.defitrack.protocol.quickswap.pooling

import io.defitrack.common.network.Network
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.pool.domain.PoolingToken
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.apr.QuickswapAPRService
import io.defitrack.quickswap.QuickswapService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@EnableScheduling
class QuickswapPoolingMarketService(
    private val quickswapService: QuickswapService,
    private val quickswapAPRService: QuickswapAPRService,
) : PoolingMarketService() {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }


    override suspend fun fetchPoolingMarkets() = quickswapService.getPairs()
        .filter {
            it.reserveUSD > BigDecimal.valueOf(100000)
        }.map {
            val element = PoolingMarketElement(
                network = getNetwork(),
                protocol = getProtocol(),
                address = it.id,
                id = "quickswap-polygon-${it.id}",
                name = "QUICKSWAP ${it.token0.symbol}-${it.token1.symbol}",
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
                apr = quickswapAPRService.getLPAPR(it.id),
                marketSize = it.reserveUSD
            )
            logger.debug("imported ${element.id}")
            element
        }

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}