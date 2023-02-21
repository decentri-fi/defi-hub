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
class LendingMarketStatisticsService(
    private val defitrackClient: DefitrackClient
) {


    val cache: Cache<String, MarketStatisticVO> = Cache.Builder()
        .expireAfterWrite(1.hours)
        .build()


    suspend fun getStatistics(): MarketStatisticVO = coroutineScope {
        cache.get("stats") {
            val protocols = defitrackClient.getProtocols()

            val marketsPerProtocol = protocols.filter {
                it.primitives.contains(DefiPrimitive.LENDING)
            }.map {
                it to async {
                    defitrackClient.getMarkets(it)
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
}