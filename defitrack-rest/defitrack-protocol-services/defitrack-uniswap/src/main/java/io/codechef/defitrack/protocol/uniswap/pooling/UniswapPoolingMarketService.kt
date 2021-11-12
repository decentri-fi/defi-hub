package io.codechef.defitrack.protocol.uniswap.pooling

import io.codechef.defitrack.pool.PoolingMarketService
import io.codechef.defitrack.pool.domain.PoolingMarketElement
import io.codechef.defitrack.pool.domain.PoolingToken
import io.codechef.defitrack.protocol.uniswap.apr.UniswapAPRService
import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.uniswap.UniswapService
import okhttp3.internal.toImmutableList
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import javax.annotation.PostConstruct

@Component
class UniswapPoolingMarketService(
    private val uniswapService: UniswapService,
    private val uniswapAPRService: UniswapAPRService,
) : PoolingMarketService {

    private val executor = Executors.newCachedThreadPool()
    private val poolingMarkets = mutableListOf<PoolingMarketElement>()

    @PostConstruct
    fun init() {
        executor.submit {
            poolingMarkets.clear()
            uniswapService.getPairs().forEach {
                val element = PoolingMarketElement(
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
                poolingMarkets.add(element)
            }
        }
    }

    override fun getPoolingMarkets(): List<PoolingMarketElement> {
        return poolingMarkets.toImmutableList()
    }

    override fun getProtocol(): Protocol {
        return Protocol.UNISWAP
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}