package io.defitrack.statistics.rest

import io.defitrack.statistics.domain.MarketStatisticVO
import io.defitrack.statistics.service.PoolingMarketStatisticsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pooling")
class PoolingMarketStatisticsRestController(
    private val poolingMarketStatisticsService: PoolingMarketStatisticsService
) {

    @GetMapping("/markets/count")
    suspend fun totalMarkets(): MarketStatisticVO {
        return poolingMarketStatisticsService.getStatistics()
    }
}