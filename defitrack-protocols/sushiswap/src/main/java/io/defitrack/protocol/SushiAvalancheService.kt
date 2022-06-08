package io.defitrack.protocol

import io.defitrack.common.network.Network
import io.defitrack.protocol.sushi.domain.PairDayData
import io.defitrack.protocol.sushi.domain.SushiswapPair
import io.defitrack.thegraph.TheGraphGatewayProvider
import io.github.reactivecircus.cache4k.Cache
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.days

@Component
class SushiAvalancheService(
    graphGatewayProvider: TheGraphGatewayProvider
) : SushiswapService {

    private val sushiswapService = SushiswapGraphGateway(
        "https://api.thegraph.com/subgraphs/name/sushiswap/avalanche-exchange",
        graphGatewayProvider
    )

    private val pairCache =
        Cache.Builder().expireAfterWrite(1.days).build<String, List<SushiswapPair>>()

    override suspend fun getPairs(): List<SushiswapPair> {
        return pairCache.get("all") {
            sushiswapService.getPairs()
        }
    }

    override suspend fun getPairDayData(pairId: String): List<PairDayData> = sushiswapService.getPairDayData(pairId)

    override suspend fun getUserPoolings(user: String) = sushiswapService.getUserPoolings(user)

    override fun getNetwork(): Network {
        return Network.AVALANCHE
    }
}