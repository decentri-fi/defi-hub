package io.defitrack.statistics.service

import io.defitrack.common.utils.AsyncUtils.await
import io.defitrack.protocol.DefiPrimitive
import io.defitrack.statistics.client.DefitrackClient
import io.defitrack.statistics.domain.MarketStatisticVO
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

@Service
class LendingMarketStatisticsService(
    private val defitrackClient: DefitrackClient
) {

    suspend fun getStatistics(): MarketStatisticVO = coroutineScope {
        val protocols = defitrackClient.getProtocols()

        val marketsPerProtocol = protocols.filter {
            it.primitives.contains(DefiPrimitive.LENDING)
        }.map {
            it to async {
                defitrackClient.getLendingMarkets(it)
            }
        }.map {
            it.first to it.second.await(3000L, emptyList())
        }

        MarketStatisticVO(
            total = marketsPerProtocol.flatMap {
                it.second
            }.count(),
            marketsPerProtocol = marketsPerProtocol.associate {
                it.first.slug to it.second.count()
            }.filter {
                it.value > 0
            }
        )
    }
}