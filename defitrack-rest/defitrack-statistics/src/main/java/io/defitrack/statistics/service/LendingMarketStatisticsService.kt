package io.defitrack.statistics.service

import io.defitrack.adapter.output.domain.market.DefiPrimitive
import io.defitrack.common.utils.AsyncUtils.await
import io.defitrack.port.output.ProtocolClient
import io.defitrack.statistics.client.DefitrackClient
import io.defitrack.statistics.domain.MarketStatisticVO
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import kotlin.time.Duration.Companion.hours

@Service
class LendingMarketStatisticsService(
    private val defitrackClient: DefitrackClient,
    private val decentrifiProtocols: ProtocolClient
) {


    val cache = Cache.Builder<String, MarketStatisticVO>()
        .expireAfterWrite(1.hours)
        .build()


    suspend fun getStatistics(): MarketStatisticVO = coroutineScope {
        cache.get("stats") {
            val protocols = decentrifiProtocols.getProtocols()

            val marketsPerProtocol = protocols.filter {
                it.primitives.contains(DefiPrimitive.LENDING)
            }.map {
                it to async {
                    defitrackClient.getLendingMarketCount(it.slug)
                }
            }.map {
                it.first to it.second.await(3000L, 0)
            }

            MarketStatisticVO(
                total = marketsPerProtocol.sumOf {
                    it.second
                },
                marketsPerProtocol = marketsPerProtocol.associate {
                    it.first.toProtocolVO() to it.second
                }.filter {
                    it.value > 0
                }
            )
        }
    }
}