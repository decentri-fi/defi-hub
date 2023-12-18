package io.defitrack.market.pooling.history

import arrow.fx.coroutines.parMap
import io.defitrack.common.network.Network
import io.defitrack.event.DefiEvent
import io.defitrack.evm.GetEventLogsCommand
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.protocol.Protocol
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PoolingHistoryAggregator(
    private val poolingMarketProvider: List<PoolingMarketProvider>,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun getPoolingHistory(protocol: Protocol, network: Network, user: String): List<DefiEvent> {
        return poolingMarketProvider
            .filter {
                it.getNetwork() == network && it.getProtocol() == protocol
            }.flatMap {
                it.getMarkets()
            }.filter {
                it.historicEventExtractor != null
            }.parMap(concurrency = 16) { market ->
                logger.info("getting events for $market")
                val extractor = market.historicEventExtractor!!
                try {
                    val gateway = blockchainGatewayProvider.getGateway(market.network)
                    gateway.getEventsAsEthLog(
                        GetEventLogsCommand(
                            addresses = extractor.addresses(),
                            topic = extractor.topic,
                            optionalTopics = extractor.optionalTopics(user),
                        )
                    ).mapNotNull {
                        val transaction = gateway.getTransaction(it.transactionHash) ?: throw IllegalArgumentException("transaction for ${it.transactionHash} on chain $network  not found")
                        extractor.toMarketEvent(it, transaction)
                    }
                } catch (ex: Exception) {
                    logger.error("Error getting events for ${market.id}", ex)
                    emptyList()
                }
            }.flatten()
    }
}