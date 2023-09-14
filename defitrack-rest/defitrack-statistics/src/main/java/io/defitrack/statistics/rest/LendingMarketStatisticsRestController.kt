package io.defitrack.statistics.rest

import io.defitrack.statistics.domain.MarketStatisticVO
import io.defitrack.statistics.service.LendingMarketStatisticsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/lending")
class LendingMarketStatisticsRestController(
    private val lendingMarketStatisticsService: LendingMarketStatisticsService
) {
    @GetMapping("/markets/count")
    suspend fun totalMarkets(): MarketStatisticVO {
        return lendingMarketStatisticsService.getStatistics()
    }
}