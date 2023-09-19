package io.defitrack.market

import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.event.EventService
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.exit.ExitPositionCommand
import io.defitrack.exit.ExitPositionPreparer
import io.defitrack.market.event.MarketAddedEvent
import io.defitrack.market.lending.domain.Position
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.price.PriceResource
import io.defitrack.protocol.CompanyProvider
import io.defitrack.protocol.ProtocolService
import io.defitrack.token.ERC20Resource
import io.defitrack.token.FungibleToken
import io.defitrack.token.MarketSizeService
import io.defitrack.transaction.PreparedTransaction
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.web3j.abi.datatypes.Function
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.system.measureTimeMillis

abstract class MarketProvider<T : DefiMarket> : ProtocolService {

    val cache = Cache.Builder<String, T>().build()

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    val semaphore = Semaphore(16)

    @Autowired
    private lateinit var erC20Resource: ERC20Resource

    @Autowired
    private lateinit var eventService: EventService

    @Autowired
    private lateinit var companyProvider: CompanyProvider

    @Autowired
    private lateinit var priceResource: PriceResource

    @Autowired
    lateinit var marketSizeService: MarketSizeService


    @Autowired
    private lateinit var blockchainGatewayProvider: BlockchainGatewayProvider


    protected open suspend fun produceMarkets(): Flow<T> {
        return emptyFlow()
    }

    protected open suspend fun fetchMarkets(): List<T> {
        return emptyList()
    }

    suspend fun refreshMarkets() = coroutineScope {
        val millis = measureTimeMillis {
            try {
                getMarkets().map { market ->
                    launch {
                        throttled {
                            market.refresh()
                        }
                    }
                }.joinAll()
            } catch (ex: Exception) {
                logger.error("something went wrong trying to refresh the cache", ex)
            }
        }
        logger.info("cache refresh took ${millis / 1000}s")
    }

    suspend fun populateCaches() {
        val millis = measureTimeMillis {
            try {
                val markets = populate()
                markets.forEach {
                    logger.debug("adding ${it.id}")
                    cache.put(it.id, it)
                    eventService.publish(
                        "markets.${it.type}.updated",
                       MarketAddedEvent.create(it)
                    )
                }

                produceMarkets().collect {
                    logger.debug("adding ${it.id}")
                    cache.put(it.id, it)
                    eventService.publish(
                        "markets.${it.type}.updated",
                        MarketAddedEvent.create(it)
                    )
                }
                logger.info("done adding ${cache.asMap().size} markets")
            } catch (ex: Exception) {
                logger.error("something went wrong trying to populate the cache", ex)
            }
        }

        logger.info("cache population took ${millis / 1000}s")
    }

    private suspend fun populate() = try {
        logger.info("Cache expired, fetching fresh elements")
        fetchMarkets()
    } catch (ex: Exception) {
        ex.printStackTrace()
        logger.error("Unable to fetch pooling markets: {}", ex.message)
        emptyList()
    }

    fun getMarkets(): List<T> {
        return try {
            val hashmap: Map<in String, T> = HashMap(cache.asMap())
            hashmap.values.toMutableList()
        } catch (ex: Exception) {
            logger.error("Unable to get markets from map", ex)
            emptyList()
        }
    }

    val chainGw: BlockchainGateway by lazy {
        blockchainGatewayProvider.getGateway(getNetwork())
    }

    fun getBlockchainGateway(): BlockchainGateway {
        return chainGw
    }

    fun defaultPositionFetcher(address: String): PositionFetcher {
        return PositionFetcher(address, { user ->
            ERC20Contract.balanceOfFunction(user)
        }, { retVal ->
            val result = retVal[0].value as BigInteger
            Position(result, result)
        })
    }

    suspend fun getToken(address: String): TokenInformationVO {
        return erC20Resource.getTokenInformation(getNetwork(), address)
    }

    suspend fun getBalance(token: String, user: String): BigInteger {
        return erC20Resource.getBalance(getNetwork(), token, user)
    }

    suspend fun getMarketSize(token: FungibleToken, location: String): BigDecimal {
        return marketSizeService.getMarketSize(token, location, getNetwork())
    }

    suspend fun getMarketSize(tokens: List<FungibleToken>, location: String): BigDecimal {
        return marketSizeService.getMarketSize(tokens, location, getNetwork())
    }

    fun getERC20Resource(): ERC20Resource {
        return erC20Resource
    }

    fun getPriceResource(): PriceResource {
        return priceResource
    }

    fun prepareExit(preparedExit: (exitPositionCommand: ExitPositionCommand) -> PreparedExit): ExitPositionPreparer {
        val network = getNetwork()
        return object : ExitPositionPreparer() {
            override suspend fun getExitPositionCommand(exitPositionCommand: ExitPositionCommand): Deferred<PreparedTransaction> {
                return coroutineScope {
                    async {
                        val prepared = preparedExit(exitPositionCommand)
                        PreparedTransaction(
                            network = getNetwork().toVO(),
                            function = prepared.function,
                            to = prepared.to,
                            from = exitPositionCommand.user
                        )
                    }
                }
            }

            override fun getNetwork(): Network {
                return network
            }
        }
    }

    data class PreparedExit(val function: Function, val to: String)

    suspend inline fun <T> throttled(action: () -> T): T {
        return semaphore.withPermit(action)
    }
}