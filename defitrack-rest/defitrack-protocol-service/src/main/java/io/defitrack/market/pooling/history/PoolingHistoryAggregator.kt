package io.defitrack.market.pooling.history

import arrow.fx.coroutines.parMap
import io.defitrack.common.network.Network
import io.defitrack.event.DefiEvent
import io.defitrack.evm.GetEventLogsCommand
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class PoolingHistoryAggregator(
    private val poolingMarketProvider: List<PoolingMarketProvider>,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun getPoolingHistory(getPoolingHistoryCommand: GetHistoryCommand): List<PoolingMarketEvent> {
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
                logger.info("getting events for $market")
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

data class PoolingMarketEvent(
    val poolingmarket: PoolingMarket,
    val event: DefiEvent
)

data class GetHistoryCommand(
    val protocol: Protocol,
    val network: Network,
    val user: String,
    val fromBlock: BigInteger?,
    val toBlock: BigInteger?,
    val markets: List<String>?
)