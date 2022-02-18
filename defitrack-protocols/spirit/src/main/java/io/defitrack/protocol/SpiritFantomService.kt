package io.defitrack.protocol

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.common.network.Network
import io.defitrack.protocol.sushi.domain.PairDayData
import io.defitrack.protocol.sushi.domain.SushiswapPair
import io.defitrack.thegraph.TheGraphGatewayProvider
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

@Component
class SpiritFantomService(
    objectMapper: ObjectMapper,
    graphGatewayProvider: TheGraphGatewayProvider
) : SpiritswapService {

    private val spiritswapService = SpiritGraphGateway(
        graphGatewayProvider.createTheGraphGateway("https://api.thegraph.com/subgraphs/name/moneyspirits/spiritswap"),
        objectMapper
    )

    fun getMasterchef() = "0x9083ea3756bde6ee6f27a6e996806fbd37f6f093"

    private val pairCache =
        Cache.Builder().expireAfterWrite(1.days).build<String, List<SushiswapPair>>()

    override fun getPairs(): List<SushiswapPair> {
        return runBlocking {
            pairCache.get("all") {
                spiritswapService.getPairs()
            }
        }
    }

    override fun getPairDayData(pairId: String): List<PairDayData> = spiritswapService.getPairDayData(pairId)

    override fun getUserPoolings(user: String) = spiritswapService.getUserPoolings(user)

    override fun getNetwork(): Network {
        return Network.FANTOM
    }
}