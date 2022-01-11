package io.defitrack.protocol.balancer.pooling

import io.defitrack.common.network.Network
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.BalancerPolygonService
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Service
class BalancerPolygonPoolingMarketService(private val balancerPolygonService: BalancerPolygonService) :
    PoolingMarketService {

    @OptIn(ExperimentalTime::class)
    val cache = Cache.Builder().expireAfterWrite(
        Duration.Companion.hours(4)
    ).build<String, List<PoolingMarketElement>>()

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 3)
    @PostConstruct
    fun initialize() {
        Executors.newSingleThreadExecutor().submit {
            getPoolingMarkets()
        }
    }

    override fun getPoolingMarkets(): List<PoolingMarketElement> {
        return runBlocking(Dispatchers.IO) {
            cache.get("all") {
                balancerPolygonService.getPools().mapNotNull {
                    if (it.totalLiquidity > BigDecimal.valueOf(100000)) {
                        val element = PoolingMarketElement(
                            id = "balancer-polygon-${it.id}",
                            network = getNetwork(),
                            protocol = getProtocol(),
                            address = it.address,
                            name = "${
                                it.tokens.joinToString("/") {
                                    it.symbol
                                }
                            } Pool",
                            token = emptyList(),
                            apr = BigDecimal.ZERO,
                            marketSize = it.totalLiquidity
                        )
                        logger.debug("importing ${element.id}")
                        element
                    } else {
                        null
                    }
                }
            }
        }
    }

    override fun getProtocol(): Protocol = Protocol.BALANCER

    override fun getNetwork(): Network = Network.POLYGON
}