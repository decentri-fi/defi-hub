package io.defitrack.protocol.sushiswap.pooling

import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.pool.domain.PoolingToken
import io.defitrack.protocol.sushiswap.apr.SushiswapAPRService
import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SushiswapService
import okhttp3.internal.toImmutableList
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.concurrent.Executors
import javax.annotation.PostConstruct

@Component
class SushiswapEthereumPoolingMarketService(
    private val sushiServices: List<SushiswapService>,
    private val sushiAPRService: SushiswapAPRService,
) : PoolingMarketService {

    private val executor = Executors.newCachedThreadPool()
    private val poolingMarkets = mutableListOf<PoolingMarketElement>()

    @PostConstruct
    fun init() {
        executor.submit {
            poolingMarkets.clear()
            sushiServices.filter {
                it.getNetwork() == getNetwork()
            }.forEach { service ->
                service.getPairs().forEach {
                    if (it.reserveUSD > BigDecimal.valueOf(100000)) {
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
                            id = "sushi-ethereum-${it.id}",
                            marketSize = it.reserveUSD
                        )
                        poolingMarkets.add(element)
                    }
                }
            }
        }
    }

    override fun getPoolingMarkets(): List<PoolingMarketElement> {
        return poolingMarkets.toImmutableList()
    }

    override fun getProtocol(): Protocol {
        return Protocol.SUSHISWAP
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}