package io.defitrack.protocol

import io.defitrack.common.network.Network
import io.defitrack.protocol.sushi.domain.SushiswapPair
import io.defitrack.thegraph.TheGraphGatewayProvider
import io.github.reactivecircus.cache4k.Cache
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.days

@Component
class SpookyFantomService(
    theGraphGatewayProvider: TheGraphGatewayProvider
) : SpookyswapService {

    private val spookyService = SpookyGraphGateway(
        "https://api.thegraph.com/subgraphs/name/sushiswap/fantom-exchange",
        theGraphGatewayProvider
    )

    fun getMasterchef() = "0x2b2929e785374c651a81a63878ab22742656dcdd"

    private val pairCache = Cache.Builder<String, List<SushiswapPair>>().expireAfterWrite(1.days).build()

    override suspend fun getPairs() = pairCache.get("all") {
        spookyService.getPairs()
    }

    override suspend fun getPairDayData(pairId: String) = spookyService.getPairDayData(pairId)

    override suspend fun getUserPoolings(user: String) = spookyService.getUserPoolings(user)

    override fun getNetwork() = Network.FANTOM
}