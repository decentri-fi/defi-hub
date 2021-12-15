package io.defitrack.protocol.uniswap.pooling

import io.defitrack.common.network.Network
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.pool.domain.PoolingToken
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.uniswap.apr.UniswapAPRService
import io.defitrack.uniswap.UniswapService
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Component
class UniswapPoolingMarketService(
    private val uniswapService: UniswapService,
    private val uniswapAPRService: UniswapAPRService,
) : PoolingMarketService {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @OptIn(ExperimentalTime::class)
    private val cache = Cache.Builder().expireAfterWrite(
        Duration.Companion.hours(4)
    ).build<String, List<PoolingMarketElement>>()

    @PostConstruct
    @Scheduled(fixedDelay = 1000 * 60 * 60 * 3)
    fun intitialPopulation() {
        logger.debug("Fetching pooling markets")
        Executors.newSingleThreadExecutor().submit {
            getPoolingMarkets()
        }
    }

    fun fetchPoolingMarkets(): List<PoolingMarketElement> {
        return uniswapService.getPairs().mapNotNull {
            try {
                PoolingMarketElement(
                    network = getNetwork(),
                    protocol = getProtocol(),
                    id = "uniswap-ethereum-${it.id}",
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

    override fun getPoolingMarkets(): List<PoolingMarketElement> {
        return runBlocking {
            cache.get("all") {
                fetchPoolingMarkets()
            }
        }
    }


    override fun getProtocol(): Protocol {
        return Protocol.UNISWAP
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}