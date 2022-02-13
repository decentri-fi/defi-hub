package io.defitrack.protocol

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.common.network.Network
import io.defitrack.protocol.sushi.domain.SushiswapPair
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Component
class SushiArbitrumService(
    objectMapper: ObjectMapper,
    client: HttpClient
) : SushiswapService {

    companion object {
        fun getMiniChefs() = listOf("0xf4d73326c13a4fc5fd7a064217e12780e9bd62c3")
    }

    private val sushiswapService = SushiswapGraphGateway(
        objectMapper,
        "https://api.thegraph.com/subgraphs/name/sushiswap/arbitrum-exchange",
        client
    )

    @OptIn(ExperimentalTime::class)
    private val pairCache =
        Cache.Builder().expireAfterWrite(Duration.Companion.days(1)).build<String, List<SushiswapPair>>()

    override fun getPairs(): List<SushiswapPair> {
        return runBlocking {
            pairCache.get("all") {
                sushiswapService.getPairs()
            }
        }
    }

    override fun getPairDayData(pairId: String) = sushiswapService.getPairDayData(pairId)

    override fun getUserPoolings(user: String) = sushiswapService.getUserPoolings(user)

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}