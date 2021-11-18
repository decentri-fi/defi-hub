package io.defitrack.protocol.dfyn.pooling

import io.codechef.defitrack.pool.PoolingMarketService
import io.codechef.defitrack.pool.domain.PoolingMarketElement
import io.codechef.defitrack.pool.domain.PoolingToken
import io.defitrack.protocol.dfyn.apr.DfynAPRService
import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.dfyn.DfynService
import okhttp3.internal.toImmutableList
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.concurrent.Executors
import javax.annotation.PostConstruct

@Component
class DfynPoolingMarketService(
    private val dfynService: DfynService,
    private val dfynAPRService: DfynAPRService,
) : PoolingMarketService {

    private val executor = Executors.newCachedThreadPool()
    private val poolingMarkets = mutableListOf<PoolingMarketElement>()

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @PostConstruct
    fun init() {
        executor.submit {
            poolingMarkets.clear()
            dfynService.getPairs().forEach {
                if (it.reserveUSD > BigDecimal.valueOf(100000)) {
                    val element = PoolingMarketElement(
                        network = getNetwork(),
                        protocol = getProtocol(),
                        address = it.id,
                        id = "polygon-dfyn-${it.id}",
                        name = "DFYN ${it.token0.symbol}-${it.token1.symbol}",
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
                        apr = dfynAPRService.getAPR(it.id),
                        marketSize = it.reserveUSD
                    )
                    logger.info("Imported ${element.id}")
                    poolingMarkets.add(element)
                }
            }
        }
    }

    override fun getPoolingMarkets(): List<PoolingMarketElement> {
        return poolingMarkets.toImmutableList()
    }

    override fun getProtocol(): Protocol {
        return Protocol.DFYN
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}