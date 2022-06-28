package io.defitrack.farming

import io.defitrack.farming.domain.MarketStatisticVO
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.time.Duration.Companion.hours

@RestController
@RequestMapping("/farming")
class FarmingMarketStatisticsRestController(
    private val farmingMarketStatisticsService: FarmingMarketStatisticsService
) {

    val cache: Cache<String, MarketStatisticVO> = Cache.Builder()
        .expireAfterWrite(1.hours)
        .build()

    @GetMapping("/markets/count")
    fun totalMarkets(): MarketStatisticVO = runBlocking {
        cache.get("stats") {
            farmingMarketStatisticsService.getStatistics()
        }
    }
}