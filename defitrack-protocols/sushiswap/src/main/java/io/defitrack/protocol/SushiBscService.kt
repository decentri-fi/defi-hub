package io.defitrack.protocol

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.common.network.Network
import io.defitrack.protocol.sushi.domain.PairDayData
import io.defitrack.protocol.sushi.domain.SushiswapPair
import io.defitrack.thegraph.TheGraphGatewayProvider
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.days

@Component
class SushiBscService(
    objectMapper: ObjectMapper,
    graphGatewayProvider: TheGraphGatewayProvider
) : SushiswapService {

    private val sushiswapService = SushiswapGraphGateway(
        objectMapper,
        "https://api.thegraph.com/subgraphs/name/sushiswap/bsc-exchange",
        graphGatewayProvider
    )

    private val pairCache =
        Cache.Builder().expireAfterWrite(1.days).build<String, List<SushiswapPair>>()

    override fun getPairs(): List<SushiswapPair> {
        return runBlocking {
            pairCache.get("all") {
                sushiswapService.getPairs()
            }
        }
    }

    override fun getPairDayData(pairId: String): List<PairDayData> = sushiswapService.getPairDayData(pairId)

    override fun getUserPoolings(user: String) = sushiswapService.getUserPoolings(user)

    override fun getNetwork(): Network {
        return Network.BINANCE
    }
}