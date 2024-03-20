package io.defitrack.protocol.sushiswap

import io.defitrack.common.network.Network
import io.defitrack.protocol.sushiswap.domain.PairDayData
import io.defitrack.protocol.sushiswap.domain.SushiswapPair
import io.defitrack.protocol.thegraph.TheGraphGatewayProvider
import io.github.reactivecircus.cache4k.Cache
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.days

@Component
class SushiArbitrumService(
    graphGatewayProvider: TheGraphGatewayProvider
) : SushiswapService {

    companion object {
        fun getMiniChefs() = listOf("0xf4d73326c13a4fc5fd7a064217e12780e9bd62c3")
    }

    private val sushiswapService = SushiswapGraphGateway(
        "https://api.thegraph.com/subgraphs/name/sushiswap/arbitrum-exchange",
        graphGatewayProvider
    )

    private val pairCache =
        Cache.Builder<String, List<SushiswapPair>>().expireAfterWrite(1.days).build()

    override suspend fun getPairs(): List<SushiswapPair> {
        return pairCache.get("all") {
            sushiswapService.getPairs()
        }
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}