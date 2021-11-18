package io.defitrack.protocol.dmm

import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.pool.domain.PoolingToken
import io.defitrack.protocol.dmm.apr.DMMAPRService
import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import okhttp3.internal.toImmutableList
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.Executors
import javax.annotation.PostConstruct

@Service
class DMMPolygonPoolingMarketService(
    private val dmmPolygonService: DMMPolygonService,
    private val dmmaprService: DMMAPRService
) : PoolingMarketService {

    val marketBuffer = mutableListOf<PoolingMarketElement>()
    val executor = Executors.newCachedThreadPool()

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @PostConstruct
    fun init() {
        marketBuffer.clear()
        dmmPolygonService.getPoolingMarkets().forEach {
            executor.submit {
                val marketElement = PoolingMarketElement(
                    id = "dmm-polygon-${it.id}",
                    network = getNetwork(),
                    protocol = getProtocol(),
                    address = it.id,
                    name = "DMM ${it.token0.symbol}-${it.token1.symbol}",
                    token = listOf(
                        PoolingToken(
                            name = it.token0.name,
                            symbol = it.token0.symbol,
                            address = it.token0.id
                        ),
                        PoolingToken(
                            name = it.token1.name,
                            symbol = it.token1.symbol,
                            address = it.token1.id
                        )
                    ),
                    apr = dmmaprService.getAPR(it.pair.id, getNetwork()),
                    marketSize = it.reserveUSD
                )
                logger.info("imported ${marketElement.id}")
                marketBuffer.add(marketElement)
            }
        }
    }

    override fun getPoolingMarkets(): List<PoolingMarketElement> {
        return marketBuffer.toImmutableList()
    }

    override fun getProtocol(): Protocol {
        return Protocol.DMM
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}