package io.defitrack.statistics.service

import io.defitrack.statistics.client.DefitrackClient
import io.defitrack.statistics.domain.MarketStatisticVO
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.hours

@Component
class AggregatedMarketStatisticsService(
    private val defitrackClient: DefitrackClient
) {

    val cache = Cache.Builder<String, MarketStatisticVO>()
        .expireAfterWrite(1.hours)
        .build()


    suspend fun getStatistics(): MarketStatisticVO = withContext(Dispatchers.IO) {
        cache.get("stats") {
            val protocols = defitrackClient.getProtocols()

            val marketsPerProtocol = protocols
                .map {
                    it to listOf(
                        async {
                            defitrackClient.getPoolingMarketsCount(it)
                        },
                        async {
                            defitrackClient.getFarmingMarketsCount(it)
                        },
                        async {
                            defitrackClient.getLendingMarketCount(it)
                        }
                    )
                }.map {
                    it.first to it.second.awaitAll()
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