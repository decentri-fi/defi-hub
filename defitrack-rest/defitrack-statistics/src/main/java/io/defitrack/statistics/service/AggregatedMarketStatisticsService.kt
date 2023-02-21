package io.defitrack.statistics.service

import io.defitrack.statistics.client.DefitrackClient
import io.defitrack.statistics.domain.MarketStatisticVO
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.*
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.hours

@Component
class AggregatedMarketStatisticsService(
    private val defitrackClient: DefitrackClient
) {

    val cache: Cache<String, MarketStatisticVO> = Cache.Builder()
        .expireAfterWrite(1.hours)
        .build()


    suspend fun getStatistics(): MarketStatisticVO = withContext(Dispatchers.IO) {
        cache.get("stats") {
            val protocols = defitrackClient.getProtocols()

            val marketsPerProtocol = protocols
                .map {
                    it to listOf(
                        async {
                            defitrackClient.getPoolingMarkets(it)
                        },
                        async {
                            defitrackClient.getFarmingMarkets(it)
                        },
                        async {
                            defitrackClient.getLendingMarkets(it)
                        }
                    )
                }.map {
                    it.first to it.second.awaitAll().flatten()
                }

            MarketStatisticVO(
                total = marketsPerProtocol.flatMap {
                    it.second
                }.count(),
                marketsPerProtocol = marketsPerProtocol.associate {
                    it.first to it.second.count()
                }.filter {
                    it.value > 0
                }
            )
        }
    }
}