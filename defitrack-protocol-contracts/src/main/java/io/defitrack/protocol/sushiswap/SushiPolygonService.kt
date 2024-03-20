package io.defitrack.protocol.sushiswap

import io.defitrack.common.network.Network
import io.defitrack.protocol.sushiswap.domain.PairDayData
import io.defitrack.protocol.sushiswap.domain.SushiswapPair
import io.defitrack.protocol.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component

@Component
class SushiPolygonService(
    graphGatewayProvider: TheGraphGatewayProvider
) : SushiswapService {

    companion object {
        fun getMiniChefs() = listOf("0x0769fd68dfb93167989c6f7254cd0d766fb2841f")
    }

    private val sushiswapService = SushiswapGraphGateway(
        "https://api.thegraph.com/subgraphs/name/sushiswap/matic-exchange",
        graphGatewayProvider
    )

    override suspend fun getPairs(): List<SushiswapPair> = sushiswapService.getPairs()

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}