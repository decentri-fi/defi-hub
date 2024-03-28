package io.defitrack.statistics.rest

import io.defitrack.protocol.ProtocolVO
import io.defitrack.statistics.service.AggregatedMarketStatisticsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class GeneralStatisticsRestController(
    private val aggregatedMarketStatisticsService: AggregatedMarketStatisticsService,
) {

    @GetMapping("/statistics")
    suspend fun getStatistics(): StatisticsVO {
        return StatisticsVO(
            marketCount = aggregatedMarketStatisticsService.getStatistics().total
        )
    }

    @GetMapping("/statistics/per-protocol")
    suspend fun getPerProtolStats(): List<StatisticsPerProtocolVO> {
        return aggregatedMarketStatisticsService.getStatistics().marketsPerProtocol.map {
            StatisticsPerProtocolVO(
                protocol = it.key,
                marketCount = it.value
            )
        }
    }

    class StatisticsPerProtocolVO(
        val protocol: ProtocolVO,
        val marketCount: Int
    )

    class StatisticsVO(
        val marketCount: Int,
    )
}