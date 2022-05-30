package io.defitrack.protocol

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.common.network.Network
import io.defitrack.protocol.sushi.domain.PairDayData
import io.defitrack.protocol.sushi.domain.SushiswapPair
import io.defitrack.thegraph.TheGraphGatewayProvider
import io.ktor.client.*
import org.springframework.stereotype.Component

@Component
class SushiPolygonService(
    objectMapper: ObjectMapper,
    graphGatewayProvider: TheGraphGatewayProvider
) : SushiswapService {

    companion object {
        fun getMiniChefs() = listOf("0x0769fd68dfb93167989c6f7254cd0d766fb2841f")
    }

    private val sushiswapService = SushiswapGraphGateway(
        objectMapper,
        "https://api.thegraph.com/subgraphs/name/sushiswap/matic-exchange",
        graphGatewayProvider
    )

    override fun getPairs(): List<SushiswapPair> = sushiswapService.getPairs()

    override fun getPairDayData(pairId: String): List<PairDayData> = sushiswapService.getPairDayData(pairId)

    override fun getUserPoolings(user: String) = sushiswapService.getUserPoolings(user)

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}