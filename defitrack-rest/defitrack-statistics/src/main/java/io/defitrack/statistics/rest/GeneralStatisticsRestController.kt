package io.defitrack.statistics.rest

import io.defitrack.statistics.service.FarmingMarketStatisticsService
import io.defitrack.statistics.service.LendingMarketStatisticsService
import io.defitrack.statistics.service.PoolingMarketStatisticsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class GeneralStatisticsRestController(
    private val farmingMarketStatisticsService: FarmingMarketStatisticsService,
    private val lendingMarketStatisticsService: LendingMarketStatisticsService,
    private val poolingMarketStatisticsService: PoolingMarketStatisticsService
) {

    @GetMapping("/statistics")
    fun getStatistics() = runBlocking(Dispatchers.IO) {
        return@runBlocking StatisticsVO(
            marketCount = listOf(
                async {
                    lendingMarketStatisticsService.getStatistics().total
                },
                async {
                    farmingMarketStatisticsService.getStatistics().total
                },
                async {
                    poolingMarketStatisticsService.getStatistics().total
                }
            ).awaitAll().sum()
        )
    }

    class StatisticsVO(
        val marketCount: Int,
    )

}