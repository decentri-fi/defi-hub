package io.defitrack.market.lending

import arrow.core.Either
import arrow.core.None
import arrow.core.some
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.lending.domain.LendingPosition
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class DefaultLendingPositionProvider(
    val lendingMarkets: List<LendingMarketProvider>,
    val gateway: BlockchainGatewayProvider
) : LendingPositionProvider() {

    val logger = LoggerFactory.getLogger(this::class.java)

    val semaphore = Semaphore(16)

    override suspend fun getLendings(protocol: String, address: String): List<LendingPosition> = coroutineScope {
        lendingMarkets
            .filter {
                it.getProtocol().slug == protocol || it.getProtocol().name == protocol
            }
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
                            Either.catch {
                                val balance = market.positionFetcher!!.extractBalance(retVal.data)

                                if (balance.underlyingAmount > BigInteger.ONE) {
                                    LendingPosition(
                                        balance.underlyingAmount,
                                        balance.tokenAmount,
                                        market,
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
                }.awaitAll().mapNotNull {
                    it.getOrNull()
                }
            }
    }
}