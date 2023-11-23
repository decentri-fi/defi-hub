package io.defitrack.market.pooling

import arrow.core.Either.Companion.catch
import arrow.core.None
import arrow.core.some
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.pooling.domain.PoolingPosition
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class DefaultPoolingPositionProvider(
    private val poolingMarketProviders: List<PoolingMarketProvider>,
    private val gateway: BlockchainGatewayProvider,
) : PoolingPositionProvider() {

    val logger = LoggerFactory.getLogger(this::class.java)

    val semaphore = Semaphore(16)

    override suspend fun fetchUserPoolings(protocol: String, address: String): List<PoolingPosition> = coroutineScope {
        poolingMarketProviders
            .filter { it.getProtocol().slug == protocol }
            .flatMap { provider ->
                val markets = provider.getMarkets().filter { it.positionFetcher != null }
                if (markets.isEmpty()) {
                    return@flatMap emptyList()
                }

                gateway.getGateway(provider.getNetwork()).readMultiCall(
                    markets.map { market ->
                        market.positionFetcher!!.functionCreator(address)
                    }
                ).mapIndexed { index, retVal ->
                    async {
                        semaphore.withPermit {
                            val market = markets[index]
                            catch {
                                val position = market.positionFetcher!!.extractBalance(retVal.data)

                                if (position.underlyingAmount > BigInteger.ONE) {
                                    PoolingPosition(
                                        position.tokenAmount,
                                        market
                                    ).some()
                                } else {
                                    None
                                }
                            }.mapLeft {
                                logger.error("Error fetching balance for ${market.name}", it)
                            }.map {
                                it.getOrNull()
                            }
                        }
                    }
                }
            }
    }.awaitAll().mapNotNull {
        it.getOrNull()
    }
}