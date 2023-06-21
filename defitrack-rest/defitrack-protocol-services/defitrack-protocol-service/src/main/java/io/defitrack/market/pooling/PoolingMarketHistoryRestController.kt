package io.defitrack.market.pooling

import io.defitrack.event.DefiEvent
import io.defitrack.evm.contract.BlockchainGateway
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pooling/history")
class PoolingMarketHistoryRestController(
    private val poolingMarketProvider: List<PoolingMarketProvider>
) {

    @GetMapping("/{user}")
    fun getEnterMarketEvents(@PathVariable("user") user: String): List<DefiEvent> = runBlocking {
        poolingMarketProvider.filter {
            it.historicEventExtractor() != null
        }.flatMap {
            val historicEventExtractor = it.historicEventExtractor()!!
            val markets = it.getMarkets()

            it.getBlockchainGateway().getEventsAsEthLog(
                BlockchainGateway.GetEventLogsCommand(
                    addresses = historicEventExtractor.addresses(markets),
                    historicEventExtractor.topic,
                    historicEventExtractor.optionalTopics(user),
                )
            ).map {
                historicEventExtractor.toMarketEvent(it)
            }
        }
    }
}