package io.defitrack.market.pooling

import arrow.core.raise.option
import com.github.michaelbull.retry.policy.binaryExponentialBackoff
import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.policy.plus
import com.github.michaelbull.retry.retry
import io.defitrack.event.DefiEvent
import io.defitrack.evm.GetEventLogsCommand
import io.defitrack.evm.contract.BlockchainGatewayProvider
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.Exception

@RestController
@RequestMapping("/{protocol}/pooling")
class PoolingMarketHistoryRestController(
    private val poolingMarketProvider: List<PoolingMarketProvider>,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{id}/history/{user}")
    suspend fun getEnterMarketEvents(
        @PathVariable("protocol") protocol: String,
        @PathVariable("user") user: String,
        @PathVariable("id") poolId: String,
    ): List<DefiEvent> {

        val market = poolingMarketProvider.flatMap {
            it.getMarkets()
        }.find {
            it.id == poolId
        }
        val extractor = market?.historicEventExtractor
        return try {
            if (extractor != null) {
                blockchainGatewayProvider.getGateway(market.network).getEventsAsEthLog(
                    GetEventLogsCommand(
                        addresses = extractor.addresses(),
                        topic = extractor.topic,
                        optionalTopics = extractor.optionalTopics(user),
                    )
                ).mapNotNull {
                    extractor.toMarketEvent(it)
                }
            } else {
                emptyList()
            }
        } catch (ex: Exception) {
            logger.error("Error getting events for $poolId", ex)
            emptyList()
        }
    }
}