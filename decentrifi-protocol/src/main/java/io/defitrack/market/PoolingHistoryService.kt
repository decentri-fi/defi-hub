package io.defitrack.market

import arrow.fx.coroutines.parMap
import io.defitrack.evm.GetEventLogsCommand
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.domain.GetHistoryCommand
import io.defitrack.market.domain.PoolingMarketEvent
import io.defitrack.market.port.`in`.PoolingHistory
import io.defitrack.market.port.out.PoolingMarketProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PoolingHistoryService(
    private val poolingMarketProvider: List<PoolingMarketProvider>,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) : PoolingHistory {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun getPoolingHistory(getPoolingHistoryCommand: GetHistoryCommand): List<PoolingMarketEvent> {
        val gateway = blockchainGatewayProvider.getGateway(getPoolingHistoryCommand.network)

        return poolingMarketProvider
            .filter {
                it.getNetwork() == getPoolingHistoryCommand.network && it.getProtocol() == getPoolingHistoryCommand.protocol
            }.flatMap {
                it.getMarkets()
            }
            .filter {
                getPoolingHistoryCommand.markets == null || getPoolingHistoryCommand.markets.contains(it.id)
            }
            .filter {
                it.historicEventExtractor != null
            }.parMap(concurrency = 32) { market ->
                logger.debug("getting events for {}", market)
                val extractor = market.historicEventExtractor!!
                try {
                    gateway.getEventsAsEthLog(
                        GetEventLogsCommand(
                            addresses = extractor.addresses(),
                            topic = extractor.topic,
                            optionalTopics = extractor.optionalTopics(getPoolingHistoryCommand.user),
                        )
                    ).map {
                        val transaction = gateway.getTransaction(it.transactionHash)
                            ?: throw IllegalArgumentException("transaction for ${it.transactionHash} on chain ${getPoolingHistoryCommand.network}  not found")
                        PoolingMarketEvent(
                            market,
                            extractor.toMarketEvent(it, transaction)
                        )
                    }
                } catch (ex: Exception) {
                    logger.error("Error getting events for ${market.id}", ex)
                    emptyList()
                }
            }.flatten()
    }
}


