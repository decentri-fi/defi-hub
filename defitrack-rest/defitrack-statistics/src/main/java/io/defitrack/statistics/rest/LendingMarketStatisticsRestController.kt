package io.defitrack.statistics.rest

import io.defitrack.statistics.domain.MarketStatisticVO
import io.defitrack.statistics.service.LendingMarketStatisticsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/lending")
class LendingMarketStatisticsRestController(
    private val lendingMarketStatisticsService: LendingMarketStatisticsService
) {

    @GetMapping("/markets/count")
    fun totalMarkets(): MarketStatisticVO = runBlocking(Dispatchers.IO) {
        lendingMarketStatisticsService.getStatistics()
    }
}