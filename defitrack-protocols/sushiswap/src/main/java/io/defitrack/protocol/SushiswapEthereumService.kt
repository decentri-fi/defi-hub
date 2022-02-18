package io.defitrack.protocol

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.common.network.Network
import io.defitrack.protocol.sushi.domain.PairDayData
import io.defitrack.protocol.sushi.domain.SushiswapPair
import io.ktor.client.*
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class SushiswapEthereumService(
    objectMapper: ObjectMapper,
    client: HttpClient
) : SushiswapService {

    private val sushiswapService = SushiswapGraphGateway(
        objectMapper,
        "https://api.thegraph.com/subgraphs/name/sushiswap/exchange",
        client
    )

    @Cacheable(cacheNames = ["sushiswap-pairs"], key = "'all-ethereum'")
    override fun getPairs(): List<SushiswapPair> = sushiswapService.getPairs()

    override fun getPairDayData(pairId: String): List<PairDayData> = sushiswapService.getPairDayData(pairId)

    override fun getUserPoolings(user: String) = sushiswapService.getUserPoolings(user)

    final override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}