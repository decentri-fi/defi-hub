package io.defitrack.statistics.service

import io.defitrack.common.utils.AsyncUtils.await
import io.defitrack.protocol.DefiPrimitive
import io.defitrack.statistics.client.DefitrackClient
import io.defitrack.statistics.domain.MarketStatisticVO
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import kotlin.time.Duration.Companion.hours

@Service
class PoolingMarketStatisticsService(
    private val defitrackClient: DefitrackClient
) {

    val cache = Cache.Builder<String, MarketStatisticVO>()
        .expireAfterWrite(1.hours)
        .build()

    suspend fun getStatistics(): MarketStatisticVO = coroutineScope {
        cache.get("stats") {
            val protocols = defitrackClient.getProtocols()

            val marketsPerProtocol = protocols.filter {
                it.primitives.contains(DefiPrimitive.POOLING)
            }.map {
                it to async {
                    defitrackClient.getPoolingMarketsCount(it)
                }
            }.map {
                it.first to it.second.await(3000L, 0)
            }

            MarketStatisticVO(
                total = marketsPerProtocol.sumOf {
                    it.second
                },
                marketsPerProtocol = marketsPerProtocol.associate {
                    it.first to it.second
                }.filter {
                    it.value > 0
                }
            )
        }
    }
}