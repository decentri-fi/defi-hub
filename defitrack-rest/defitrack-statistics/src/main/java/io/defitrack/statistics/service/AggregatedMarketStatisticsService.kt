package io.defitrack.statistics.service

import io.defitrack.port.output.ProtocolClient
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
    private val defitrackClient: DefitrackClient,
    private val decentrifiProtocols: ProtocolClient,
) {

    val cache = Cache.Builder<String, MarketStatisticVO>()
        .expireAfterWrite(1.hours)
        .build()


    suspend fun getStatistics(): MarketStatisticVO = withContext(Dispatchers.IO) {
        cache.get("stats") {
            val protocols = decentrifiProtocols.getProtocols()

            val marketsPerProtocol = protocols
                .map {
                    it to listOf(
                        async {
                            defitrackClient.getPoolingMarketsCount(it.slug)
                        },
                        async {
                            defitrackClient.getFarmingMarketsCount(it.slug)
                        },
                        async {
                            defitrackClient.getLendingMarketCount(it.slug)
                        }
                    )
                }.map {
                    it.first to it.second.awaitAll()
                }

            MarketStatisticVO(
                total = marketsPerProtocol.flatMap {
                    it.second
                }.sum(),
                marketsPerProtocol = marketsPerProtocol.associate {
                    it.first.toProtocolVO() to it.second.sum()
                }.filter {
                    it.value > 0
                }
            )
        }
    }
}