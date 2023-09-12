package io.defitrack.market.pooling

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.pooling.domain.PoolingPosition
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class DefaultPoolingPositionProvider(
    private val poolingMarketProviders: List<PoolingMarketProvider>,
    private val gateway: BlockchainGatewayProvider,
) : PoolingPositionProvider() {

    val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun fetchUserPoolings(protocol: String, address: String): List<PoolingPosition> = coroutineScope {
        poolingMarketProviders
            .filter {
                it.getProtocol().slug == protocol
            }
            .map { provider ->
                return@map async {
                    try {
                        val markets = provider.getMarkets().filter { it.positionFetcher != null }

                        if (markets.isEmpty()) {
                            return@async emptyList()
                        }

                        gateway.getGateway(provider.getNetwork()).readMultiCall(
                            markets.map { market ->
                                market.positionFetcher!!.toMulticall(address)
                            }
                        ).mapIndexed { index, retVal ->
                            val market = markets[index]
                            val position = market.positionFetcher!!.extractBalance(retVal.data)

                            if (position.underlyingAmount > BigInteger.ONE) {
                                PoolingPosition(
                                    position.tokenAmount,
                                    market
                                )
                            } else {
                                null
                            }
                        }.filterNotNull()
                    } catch (ex: Exception) {
                        logger.error("Unable to fetch user poolings for provider ${provider.javaClass}: ${ex.message}")
                        emptyList()
                    }
                }
            }.awaitAll().flatten()
    }

}