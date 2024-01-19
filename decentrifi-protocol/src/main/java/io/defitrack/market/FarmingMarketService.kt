package io.defitrack.market

import arrow.core.Either
import arrow.core.None
import arrow.core.some
import io.defitrack.common.network.Network
import io.defitrack.erc20.port.`in`.ERC20Resource
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.market.domain.farming.FarmingPosition
import io.defitrack.market.port.`in`.FarmingMarkets
import io.defitrack.market.port.`in`.FarmingPositions
import io.defitrack.market.port.out.FarmingPositionProvider
import io.defitrack.market.port.out.MarketProvider
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.web3j.crypto.WalletUtils
import java.math.BigInteger

@Component
class FarmingMarketService(
    private val marketProviders: List<MarketProvider<FarmingMarket>>,
    private val farmingPositionProviders: List<FarmingPositionProvider>,
    private val gateway: BlockchainGatewayProvider,
    private val erC20Resource: ERC20Resource
) : FarmingMarkets, FarmingPositions {

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
    ): List<FarmingMarket> {
        return marketProviders.filter {
            it.getProtocol().slug == protocol
        }.filter {
            it.getNetwork() == network
        }.flatMap {
            it.getMarkets()
        }.filter {
            val token = erC20Resource.getTokenInformation(it.network, it.stakedToken.address.lowercase())
            if (token.type != TokenType.SINGLE) {
                it.stakedToken.address.lowercase() == tokenAddress.lowercase() || token.underlyingTokens.any { underlyingToken ->
                    underlyingToken.address.lowercase() == tokenAddress.lowercase()
                }
            } else {
                it.stakedToken.address.lowercase() == tokenAddress.lowercase()
            }
        }
    }

    override fun getStakingMarketById(id: String) = marketProviders.flatMap {
        it.getMarkets()
    }.firstOrNull { it.id == id }


    private val logger = LoggerFactory.getLogger(this::class.java)

    val semaphore = Semaphore(16)

    override suspend fun getPositions(
        protocol: String,
        address: String
    ): List<FarmingPosition> {
        return if (WalletUtils.isValidAddress(address)) {
            fromProviders(protocol, address) + fromMarkets(protocol, address)
        } else {
            emptyList()
        }
    }

    private suspend fun fromProviders(
        protocol: String,
        address: String
    ) = farmingPositionProviders
        .flatMap {
            try {
                it.getStakings(protocol, address).filter {
                    it.underlyingAmount > BigInteger.ZERO
                }
            } catch (ex: Exception) {
                logger.error("Something went wrong trying to fetch the user stakings: ${ex.message}")
                emptyList()
            }
        }

    suspend fun fromMarkets(protocol: String, address: String): List<FarmingPosition> = coroutineScope {
        marketProviders.filter {
            it.getProtocol().slug == protocol
        }.flatMap { provider ->
            val markets = provider.getMarkets().filter { it.balanceFetcher != null }
            if (markets.isEmpty()) {
                return@flatMap emptyList()
            }

            gateway.getGateway(provider.getNetwork()).readMultiCall(
                markets.map { market ->
                    market.balanceFetcher!!.functionCreator(address)
                }
            ).mapIndexed { index, retVal ->
                async {
                    semaphore.withPermit {
                        val market = markets[index]
                        Either.catch {

                            if (!retVal.success) {
                                logger.info("Call to get position returned error ${market.name}")
                                return@catch None
                            }

                            val balance = market.balanceFetcher!!.extractBalance(retVal.data)

                            if (balance.underlyingAmount > BigInteger.ONE) {
                                FarmingPosition(
                                    market,
                                    balance.underlyingAmount,
                                    balance.tokenAmount
                                ).some()
                            } else {
                                None
                            }
                        }.mapLeft {
                            logger.error("Error fetching balance for ${market.name}: {}", it.message)
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