package io.defitrack.protocol.sushiswap

import io.defitrack.common.network.Network
import io.defitrack.protocol.sushiswap.domain.PairDayData
import io.defitrack.protocol.sushiswap.domain.SushiswapPair
import io.defitrack.protocol.thegraph.TheGraphGatewayProvider
import io.github.reactivecircus.cache4k.Cache
import org.springframework.stereotype.Service
import kotlin.time.Duration.Companion.days

@Service
class SushiswapEthereumService(
    graphGatewayProvider: TheGraphGatewayProvider
) : SushiswapService {

    private val sushiswapService = SushiswapGraphGateway(
        "https://api.thegraph.com/subgraphs/name/sushiswap/exchange",
        graphGatewayProvider
    )

    private val pairCache =
        Cache.Builder<String, List<SushiswapPair>>().expireAfterWrite(1.days).build()

    override suspend fun getPairs(): List<SushiswapPair> {
        return pairCache.get("all") {
            sushiswapService.getPairs()
        }
    }

    final override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}