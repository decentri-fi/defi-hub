package io.defitrack.statistics.rest

import io.defitrack.statistics.domain.MarketStatisticVO
import io.defitrack.statistics.service.FarmingMarketStatisticsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/farming")
class FarmingMarketStatisticsRestController(
    private val farmingMarketStatisticsService: FarmingMarketStatisticsService
) {

    @GetMapping("/markets/count")
    suspend fun totalMarkets(): MarketStatisticVO  {
       return farmingMarketStatisticsService.getStatistics()
    }
}