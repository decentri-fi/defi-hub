package io.defitrack.protocol

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.common.network.Network
import io.defitrack.protocol.sushi.domain.SushiswapPair
import io.ktor.client.*
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class SushiPolygonService(objectMapper: ObjectMapper,
                          private val client: HttpClient
) : SushiswapService {

    private val sushiswapService = SushiswapGraphGateway(
        objectMapper,
        "https://api.thegraph.com/subgraphs/name/sushiswap/matic-exchange",
        client
    )

    @Cacheable(cacheNames = ["sushiswap-pairs"], key = "'all-polygon'")
    override fun getPairs(): List<SushiswapPair> = sushiswapService.getPairs()

    override fun getPairDayData(pairId: String) = sushiswapService.getPairDayData(pairId)

    override fun getUserPoolings(user: String) = sushiswapService.getUserPoolings(user)

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}