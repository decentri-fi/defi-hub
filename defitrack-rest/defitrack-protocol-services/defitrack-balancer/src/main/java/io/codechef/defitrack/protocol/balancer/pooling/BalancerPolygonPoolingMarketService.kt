package io.codechef.defitrack.protocol.balancer.pooling

import io.codechef.defitrack.pool.PoolingMarketService
import io.codechef.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.BalancerPolygonService
import okhttp3.internal.toImmutableList
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import javax.annotation.PostConstruct

@Service
class BalancerPolygonPoolingMarketService(private val balancerPolygonService: BalancerPolygonService) :
    PoolingMarketService {

    val marketBuffer = mutableListOf<PoolingMarketElement>()

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @PostConstruct
    fun init() {
        try {
            balancerPolygonService.getPools().forEach {
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
                    logger.info("importing ${element.id}")
                    marketBuffer.add(element)
                }
            }
        } catch (ex: Exception) {
            logger.error("Error occurred at startup, balancer pools failed to load", ex);
        }
    }

    override fun getPoolingMarkets(): List<PoolingMarketElement> {
        return marketBuffer.toImmutableList()
    }

    override fun getProtocol(): Protocol = Protocol.BALANCER

    override fun getNetwork(): Network = Network.POLYGON
}