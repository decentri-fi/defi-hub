package io.defitrack.market

import arrow.core.Either
import arrow.core.None
import arrow.core.some
import arrow.fx.coroutines.parMap
import io.defitrack.common.network.Network
import io.defitrack.erc20.port.`in`.ERC20Resource
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.pooling.PoolingPosition
import io.defitrack.market.port.`in`.PoolingMarkets
import io.defitrack.market.port.`in`.PoolingPositions
import io.defitrack.market.port.out.MarketProvider
import io.defitrack.market.port.out.PoolingPositionProvider
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class PoolingMarketService(
    private val marketProviders: List<MarketProvider<PoolingMarket>>,
    private val poolingPositionProviders: List<PoolingPositionProvider>,
    private val erC20Resource: ERC20Resource,
    private val gateway: BlockchainGatewayProvider,
) : PoolingMarkets, PoolingPositions {

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

    override suspend fun searchByToken(
        protocol: String,
        tokenAddress: String,
        network: Network
    ): List<PoolingMarket> {
        return marketProviders
            .filter {
                it.getProtocol().slug == protocol
            }
            .filter {
                it.getNetwork() == network
            }
            .flatMap {
                it.getMarkets()
            }.filter {
                it.breakdown.get().any { share ->
                    share.token.address.lowercase() == tokenAddress.lowercase()
                } || it.address.lowercase() == tokenAddress.lowercase()
            }
    }

    override suspend fun findAlternatives(
        protocol: String,
        tokenAddress: String,
        network: Network
    ): List<PoolingMarket> {
        val token = erC20Resource.getTokenInformation(
            network, tokenAddress,
        )
        return marketProviders
            .filter {
                it.getProtocol().slug == protocol
            }
            .filter {
                it.getNetwork() == network
            }
            .flatMap {
                it.getMarkets()
            }.filter { poolingMarketElement ->
                when {
                    (token.type) != TokenType.SINGLE -> {
                        poolingMarketElement.breakdown.get().map { share ->
                            share.token.address.lowercase()
                        }.containsAll(
                            token.underlyingTokens.map {
                                it.address.lowercase()
                            }
                        )
                    }

                    else -> false
                }
            }
    }

    override suspend fun getUserPoolings(
        protocol: String,
        address: String
    ): List<PoolingPosition> {
        return poolingPositionProviders.parMap(concurrency = 8) {
            try {
                it.userPoolings(protocol, address)
            } catch (ex: Exception) {
                ex.printStackTrace()
                emptyList()
            }
        }.flatten() + getPoolsFromMarkets(protocol, address)
    }


    val semaphore = Semaphore(16)

    suspend fun getPoolsFromMarkets(protocol: String, address: String): List<PoolingPosition> = coroutineScope {
        marketProviders
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
                            Either.catch {
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
            }.awaitAll().mapNotNull {
                it.getOrNull()
            }
    }
}