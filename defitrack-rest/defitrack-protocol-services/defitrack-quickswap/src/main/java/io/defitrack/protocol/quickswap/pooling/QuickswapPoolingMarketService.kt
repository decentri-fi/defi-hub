package io.defitrack.protocol.quickswap.pooling

import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.pool.domain.PoolingToken
import io.defitrack.protocol.quickswap.apr.QuickswapAPRService
import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.quickswap.QuickswapService
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Component
class QuickswapPoolingMarketService(
    private val quickswapService: QuickswapService,
    private val quickswapAPRService: QuickswapAPRService,
) : PoolingMarketService {

    @OptIn(ExperimentalTime::class)
    private val cache = Cache.Builder().expireAfterWrite(
        Duration.Companion.hours(4)
    ).build<String, List<PoolingMarketElement>>()

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @PostConstruct
    @Scheduled(fixedDelay = 1000 * 60 * 60 * 3)
    fun intitialPopulation() {
        logger.debug("fetching quickswap pooling markets")
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

    private fun fetchPoolingMarkets() = quickswapService.getPairs().mapNotNull {
        if (it.reserveUSD > BigDecimal.valueOf(100000)) {
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
            logger.info("imported ${element.id}")
            element
        } else {
            null
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}