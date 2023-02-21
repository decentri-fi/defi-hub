package io.defitrack.statistics.rest

import io.defitrack.statistics.domain.MarketStatisticVO
import io.defitrack.statistics.service.PoolingMarketStatisticsService
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pooling")
class PoolingMarketStatisticsRestController(
    private val poolingMarketStatisticsService: PoolingMarketStatisticsService
) {

    @GetMapping("/markets/count")
    fun totalMarkets(): MarketStatisticVO = runBlocking {
        poolingMarketStatisticsService.getStatistics()
    }
}