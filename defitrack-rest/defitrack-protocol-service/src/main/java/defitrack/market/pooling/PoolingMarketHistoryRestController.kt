package io.defitrack.market.pooling

import com.github.michaelbull.retry.policy.binaryExponentialBackoff
import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.policy.plus
import com.github.michaelbull.retry.retry
import io.defitrack.event.DefiEvent
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.GetEventLogsCommand
import io.defitrack.market.pooling.history.PoolingHistoryProvider
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/{protocol}/pooling")
class PoolingMarketHistoryRestController(
    private val poolingHistoryProviders: List<PoolingHistoryProvider>
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{user}/history")
    suspend fun getEnterMarketEvents(
        @PathVariable("protocol") protocol: String,
        @PathVariable("user") user: String
    ): List<DefiEvent>  {
        return poolingHistoryProviders.filter {
            it.poolingMarketProvider.getProtocol().slug == protocol
        }.flatMap {
            try {
                retry(limitAttempts(3) + binaryExponentialBackoff(1000, 10000)) {
                    val historicEventExtractor = it.historicEventExtractor()

                    it.poolingMarketProvider.getBlockchainGateway().getEventsAsEthLog(
                        GetEventLogsCommand(
                            addresses = historicEventExtractor.addresses(),
                            historicEventExtractor.topic,
                            historicEventExtractor.optionalTopics(user),
                        )
                    ).mapNotNull {
                        historicEventExtractor.toMarketEvent(it)
                    }
                }
            } catch (dx: Exception) {
                logger.error("Unable to fetch historic events, dx")
                emptyList()
            }
        }
    }
}