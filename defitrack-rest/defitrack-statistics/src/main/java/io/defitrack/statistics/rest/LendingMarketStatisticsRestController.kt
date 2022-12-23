package io.defitrack.statistics.rest

import io.defitrack.statistics.domain.MarketStatisticVO
import io.defitrack.statistics.service.LendingMarketStatisticsService
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.time.Duration.Companion.hours

@RestController
@RequestMapping("/lending")
class LendingMarketStatisticsRestController(
    private val lendingMarketStatisticsService: LendingMarketStatisticsService
) {

    val cache: Cache<String, MarketStatisticVO> = Cache.Builder()
        .expireAfterWrite(1.hours)
        .build()

    @GetMapping("/markets/count")
    fun totalMarkets(): MarketStatisticVO = runBlocking {
        cache.get("stats") {
            lendingMarketStatisticsService.getStatistics()
        }
    }
}