package io.defitrack.statistics.service

import io.defitrack.statistics.client.DefitrackClient
import io.defitrack.protocol.DefiPrimitive
import io.defitrack.statistics.domain.MarketStatisticVO
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import org.springframework.stereotype.Service
import io.defitrack.common.utils.AsyncUtils.await
import io.github.reactivecircus.cache4k.Cache
import kotlin.time.Duration.Companion.hours

@Service
class FarmingMarketStatisticsService(
    private val defitrackClient: DefitrackClient
) {

    val cache: Cache<String, MarketStatisticVO> = Cache.Builder()
        .expireAfterWrite(1.hours)
        .build()

    suspend fun getStatistics(): MarketStatisticVO = coroutineScope {
      cache.get("stats") {
          val protocols = defitrackClient.getProtocols()

          val marketsPerProtocol = protocols.filter {
              it.primitives.contains(DefiPrimitive.FARMING)
          }.map {
              it to async {
                  defitrackClient.getFarmingMarkets(it)
              }
          }.map {
              it.first to it.second.await(3000L, emptyList())
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