package io.defitrack.market

import arrow.core.Either
import arrow.core.None
import arrow.core.some
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.domain.lending.LendingMarket
import io.defitrack.market.domain.lending.LendingPosition
import io.defitrack.market.port.`in`.LendingMarkets
import io.defitrack.market.port.`in`.LendingPositions
import io.defitrack.market.port.out.LendingMarketProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class LendingMarketService(
    private val marketProviders: List<LendingMarketProvider>,
    private val gateway: BlockchainGatewayProvider
) : LendingMarkets, LendingPositions {

    val logger = LoggerFactory.getLogger(this::class.java)

    override fun getAllMarkets(protocol: String, network: String?) = marketProviders
        .filter {
            network == null || it.getNetwork().slug == network || it.getNetwork().name == network
        }
        .filter {
            it.getProtocol().slug == protocol
        }
        .flatMap {
            it.getMarkets()
        }


    override fun searchByToken(
        protocol: String,
        token: String,
        network: Network
    ): List<LendingMarket> {
        return marketProviders.filter {
            it.getProtocol().slug == protocol
        }.filter {
            it.getNetwork() == network
        }.flatMap {
            it.getMarkets()
        }.filter {
            it.token.address.lowercase() == token
        }
    }

    val semaphore = Semaphore(16)
    override suspend fun getPositions(protocol: String, address: String): List<LendingPosition> {
        return coroutineScope {
            marketProviders
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


}