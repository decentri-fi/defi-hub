package io.defitrack.statistics.rest

import io.defitrack.protocol.ProtocolVO
import io.defitrack.statistics.service.AggregatedMarketStatisticsService
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class GeneralStatisticsRestController(
    private val aggregatedMarketStatisticsService: AggregatedMarketStatisticsService,
) {

    @GetMapping("/statistics")
    fun getStatistics() = runBlocking {
        return@runBlocking StatisticsVO(
            marketCount = aggregatedMarketStatisticsService.getStatistics().total
        )
    }

    @GetMapping("/statistics/per-protocol")
    fun getPerProtolStats(): List<StatisticsPerProtocol> = runBlocking {
        aggregatedMarketStatisticsService.getStatistics().marketsPerProtocol.map {
            StatisticsPerProtocol(
                protocol = it.key,
                marketCount = it.value
            )
        }
    }

    class StatisticsPerProtocol(
        val protocol: ProtocolVO,
        val marketCount: Int
    )

    class StatisticsVO(
        val marketCount: Int,
    )
}