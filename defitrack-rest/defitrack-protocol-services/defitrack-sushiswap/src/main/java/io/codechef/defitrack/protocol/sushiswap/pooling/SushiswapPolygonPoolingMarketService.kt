package io.codechef.defitrack.protocol.sushiswap.pooling

import io.codechef.common.network.Network
import io.codechef.defitrack.pool.PoolingMarketService
import io.codechef.defitrack.pool.domain.PoolingMarketElement
import io.codechef.defitrack.pool.domain.PoolingToken
import io.codechef.defitrack.protocol.sushiswap.apr.SushiswapAPRService
import io.codechef.protocol.Protocol
import io.codechef.protocol.SushiswapService
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Component
class SushiswapPolygonPoolingMarketService(
    private val sushiServices: List<SushiswapService>,
    private val sushiAPRService: SushiswapAPRService,
) : PoolingMarketService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @OptIn(ExperimentalTime::class)
    private val cache = Cache.Builder().expireAfterWrite(
        Duration.Companion.hours(4)
    ).build<String, List<PoolingMarketElement>>()

    @PostConstruct
    fun initialPopulation() {
        Executors.newSingleThreadExecutor().submit {
            getPoolingMarkets()
        }
    }

    override fun getPoolingMarkets(): List<PoolingMarketElement> {
        return runBlocking {
            cache.get("all") {
                fetchPoolingMarkets()
            }
        }
    }

    private fun fetchPoolingMarkets() = sushiServices.filter {
        it.getNetwork() == getNetwork()
    }.flatMap { service ->
        service.getPairs().map {
            val element = PoolingMarketElement(
                network = service.getNetwork(),
                protocol = getProtocol(),
                address = it.id,
                name = "SUSHI ${it.token0.symbol}-${it.token1.symbol}",
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
                apr = sushiAPRService.getAPR(it.id, service.getNetwork()),
                id = "sushi-polygon-${it.id}",
                marketSize = it.reserveUSD
            )
            logger.info("${element.id} imported")
            element
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.SUSHISWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}