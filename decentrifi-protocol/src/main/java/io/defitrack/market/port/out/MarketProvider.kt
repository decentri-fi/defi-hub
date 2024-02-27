package io.defitrack.market.port.out

import io.defitrack.company.CompaniesProvider
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.erc20.port.`in`.ERC20Resource
import io.defitrack.event.EventService
import io.defitrack.event.event.marketUpdatedEvent
import io.defitrack.evm.contract.*
import io.defitrack.evm.position.Position
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.MarketSizeService
import io.defitrack.market.domain.DefiMarket
import io.defitrack.market.domain.position.ExitPositionCommand
import io.defitrack.market.domain.position.ExitPositionPreparer
import io.defitrack.networkinfo.toNetworkInformation
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.price.port.`in`.PricePort
import io.defitrack.protocol.ProtocolService
import io.defitrack.transaction.PreparedTransaction
import io.github.reactivecircus.cache4k.Cache
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.observation.ObservationRegistry
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.system.measureTimeMillis
import kotlin.time.measureTime

abstract class MarketProvider<T : DefiMarket> : ProtocolService {

    val cache = Cache.Builder<String, T>().build()

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    val semaphore = Semaphore(16)

    @Autowired
    lateinit var erC20Resource: ERC20Resource

    @Autowired
    private lateinit var bulkConstantResolver: BulkConstantResolver

    @Autowired
    private lateinit var observationRegistry: ObservationRegistry

    @Autowired
    private lateinit var meterregisty: MeterRegistry

    @Autowired
    private lateinit var eventService: EventService

    @Autowired
    private lateinit var companyProvider: CompaniesProvider

    @Autowired
    private lateinit var priceResource: PricePort

    @Autowired
    lateinit var marketSizeService: MarketSizeService

    @Autowired
    private lateinit var blockchainGatewayProvider: BlockchainGatewayProvider

    suspend fun <T : EvmContract> List<T>.resolve(): List<T> {
        return bulkConstantResolver.resolve(this)
    }

    open fun order(): Int {
        return 1
    }

    protected open suspend fun produceMarkets(): Flow<T> {
        return emptyFlow()
    }

    protected open suspend fun fetchMarkets(): List<T> {
        return emptyList()
    }

    suspend fun refreshMarkets() {
        val millis = measureTimeMillis {
            try {
                getMarkets().forEach { market ->
                    market.refresh().also {
                        eventService.publish(
                            "markets.${it.category}.updated",
                            it.marketUpdatedEvent()
                        )
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                logger.error("something went wrong trying to refresh the cache", ex)
            }
        }
        logger.debug("cache refresh took ${millis / 1000}s")
    }

    suspend fun populateCaches() {
        val time = measureTime {
            try {
                val markets = populate()
                markets.forEach {
                    putInCache(it)
                }

                produceMarkets().collect {
                    putInCache(it)
                }
            } catch (ex: Exception) {
                logger.error("something went wrong trying to populate the cache", ex)
            }
        }

        logger.info("added ${cache.asMap().size} markets for ${getProtocol()} on ${getNetwork()} in ${time.inWholeSeconds} seconds")
    }

    private fun putInCache(it: T) {
        logger.debug("adding ${it.id}")
        cache.put(it.id, it)
        eventService.publish(
            "markets.${it.category}.updated",
            it.marketUpdatedEvent()
        )
        meterregisty.counter(
            "markets.${it.category}.added", listOf(
                Tag.of("protocol", it.protocol.slug)
            )
        ).increment()
    }

    private suspend fun populate() = try {
        logger.debug("Cache expired, fetching fresh elements")
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
        return PositionFetcher(
            { user ->
                ContractCall(
                    ERC20Contract.balanceOf(user),
                    getNetwork(),
                    address,
                )
            }
        ) { retVal ->
            val result = retVal[0].value as BigInteger
            Position(result, result)
        }
    }

    suspend fun getToken(address: String): FungibleTokenInformation {
        return erC20Resource.getTokenInformation(getNetwork(), address)
    }

    suspend fun getBalance(token: String, user: String): BigInteger {
        return erC20Resource.getBalance(getNetwork(), token, user)
    }

    suspend fun getMarketSize(token: FungibleTokenInformation, location: String): BigDecimal {
        return marketSizeService.getMarketSize(token, location, getNetwork()).usdAmount
    }

    suspend fun getMarketSize(tokens: List<FungibleTokenInformation>, location: String): BigDecimal {
        return marketSizeService.getMarketSizeInUSD(tokens, location, getNetwork())
    }

    fun getERC20Resource(): ERC20Resource {
        return erC20Resource
    }

    fun getPriceResource(): PricePort {
        return priceResource
    }

    suspend fun getPrice(token: String, amount: BigDecimal): BigDecimal {
        return getPriceResource().calculatePrice(
            GetPriceCommand(
                token,
                getNetwork(),
                amount,
            )
        ).toBigDecimal()
    }

    fun prepareExit(preparedExit: (exitPositionCommand: ExitPositionCommand) -> ContractCall): ExitPositionPreparer {
        return object : ExitPositionPreparer() {
            override suspend fun getExitPositionCommand(exitPositionCommand: ExitPositionCommand): Deferred<PreparedTransaction> {
                return coroutineScope {
                    async {
                        val prepared = preparedExit(exitPositionCommand)
                        PreparedTransaction(
                            network = getNetwork().toNetworkInformation(),
                            function = prepared.function,
                            to = prepared.address,
                            from = exitPositionCommand.user
                        )
                    }
                }
            }
        }
    }

    suspend inline fun <T> throttled(action: () -> T): T {
        return semaphore.withPermit(action)
    }

    fun FungibleTokenInformation.asERC20Contract(blockchainGateway: BlockchainGateway): ERC20Contract {
        return with(blockchainGateway) {
            ERC20Contract(address)
        }
    }
}