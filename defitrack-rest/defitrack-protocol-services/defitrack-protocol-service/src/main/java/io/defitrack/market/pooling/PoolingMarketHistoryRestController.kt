package io.defitrack.market.pooling

import io.defitrack.event.DefiEvent
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.market.pooling.history.PoolingHistoryProvider
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pooling")
class PoolingMarketHistoryRestController(
    private val poolingHistoryProviders: List<PoolingHistoryProvider>
) {

    @GetMapping("/{user}/history")
    fun getEnterMarketEvents(@PathVariable("user") user: String): List<DefiEvent> = runBlocking {
        poolingHistoryProviders.flatMap {
            val historicEventExtractor = it.historicEventExtractor()

            it.poolingMarketProvider.getBlockchainGateway().getEventsAsEthLog(
                BlockchainGateway.GetEventLogsCommand(
                    addresses = historicEventExtractor.addresses(),
                    historicEventExtractor.topic,
                    historicEventExtractor.optionalTopics(user),
                )
            ).mapNotNull {
                historicEventExtractor.toMarketEvent(it)
            }
        }
    }
}