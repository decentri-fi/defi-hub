package io.defitrack.farming

import io.defitrack.farming.domain.MarketStatisticVO
import io.defitrack.protocol.DefiPrimitive
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import org.springframework.stereotype.Service

@Service
class FarmingMarketStatisticsService(
    private val defitrackClient: DefitrackClient
) {

    suspend fun <T> Deferred<T>.await(timeout: Long, defaultValue: T) =
        withTimeoutOrNull(timeout) { await() } ?: defaultValue

    suspend fun getStatistics(): MarketStatisticVO = coroutineScope {
        val protocols = defitrackClient.getProtocols()

        val marketsPerProtocol = protocols.filter {
            it.primitives.contains(DefiPrimitive.FARMING)
        }.map {
            it to async {
                defitrackClient.getFarmingMarkets(it)
            }
        }.map {
            it.first to it.second.await(10000L, emptyList())
        }

        MarketStatisticVO(
            total = marketsPerProtocol.flatMap {
                it.second
            }.count(),
            marketsPerProtocol = marketsPerProtocol.associate {
                it.first.slug to it.second.count()
            }
        )
    }
}