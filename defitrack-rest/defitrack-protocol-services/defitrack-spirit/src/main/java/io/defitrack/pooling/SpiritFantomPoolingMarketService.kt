package io.defitrack.pooling

import io.defitrack.SpiritswapAPRService
import io.defitrack.common.network.Network
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.pool.domain.PoolingToken
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SpiritswapService
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Component
class SpiritFantomPoolingMarketService(
    private val spiritswapServices: List<SpiritswapService>,
    private val spiritswapAPRService: SpiritswapAPRService
) : PoolingMarketService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @OptIn(ExperimentalTime::class)
    private val cache = Cache.Builder().expireAfterWrite(
        Duration.Companion.hours(4)
    ).build<String, List<PoolingMarketElement>>()

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 3)
    fun initialPopulation() {
        getPoolingMarkets()
    }

    override fun getPoolingMarkets(): List<PoolingMarketElement> {
        return runBlocking(Dispatchers.IO) {
            cache.get("all") {
                fetchPoolingMarkets()
            }
        }
    }

    private fun fetchPoolingMarkets() = spiritswapServices.filter {
        it.getNetwork() == getNetwork()
    }.flatMap { service ->
        service.getPairs()
            .filter {
                it.reserveUSD > BigDecimal.valueOf(100000)
            }
            .map {
                val element = PoolingMarketElement(
                    network = service.getNetwork(),
                    protocol = getProtocol(),
                    address = it.id,
                    name = "Spirit ${it.token0.symbol}-${it.token1.symbol}",
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
                    apr = spiritswapAPRService.getAPR(it.id, service.getNetwork()),
                    id = "spirit-fantom-${it.id}",
                    marketSize = it.reserveUSD
                )
                logger.info("${element.id} imported")
                element
            }
    }

    override fun getProtocol(): Protocol {
        return Protocol.SPIRITSWAP
    }

    override fun getNetwork(): Network {
        return Network.FANTOM
    }
}