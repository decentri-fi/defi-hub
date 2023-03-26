package io.defitrack.market

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.exit.ExitPositionCommand
import io.defitrack.exit.ExitPositionPreparer
import io.defitrack.market.lending.domain.Position
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.ProtocolProvider
import io.defitrack.protocol.ProtocolService
import io.defitrack.token.ERC20Resource
import io.defitrack.token.FungibleToken
import io.defitrack.token.MarketSizeService
import io.defitrack.transaction.PreparedTransaction
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.web3j.abi.datatypes.Function
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.hours

abstract class MarketProvider<T> : ProtocolService {

    val cache = Cache.Builder().expireAfterWrite(4.hours).build<String, List<T>>()
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    val semaphore = Semaphore(8)

    @Autowired
    private lateinit var erC20Resource: ERC20Resource

    @Autowired
    private lateinit var protocolProvider: ProtocolProvider

    @Autowired
    private lateinit var priceResource: PriceResource

    @Autowired
    lateinit var marketSizeService: MarketSizeService


    @Autowired
    lateinit var abiResource: ABIResource


    @Autowired
    private lateinit var blockchainGatewayProvider: BlockchainGatewayProvider


    protected abstract suspend fun fetchMarkets(): List<T>

    fun refreshCaches() = runBlocking(Dispatchers.Default) {
        val millis = measureTimeMillis {
            try {
                val markets = populate()
                cache.put("all", markets)
                logger.info("${markets.size} markets added")
            } catch (ex: Exception) {
                logger.error("something went wrong trying to populate the cache", ex)
            }
        }

        logger.info("cache refresh took ${millis / 1000}s")
    }

    private suspend fun populate() = try {
        logger.info("Cache expired, fetching fresh elements")
        fetchMarkets()
    } catch (ex: Exception) {
        ex.printStackTrace()
        logger.error("Unable to fetch pooling markets: {}", ex.message)
        emptyList()
    }

    fun getMarkets(): List<T> = runBlocking(Dispatchers.Default) {
        cache.get("all") ?: emptyList()
    }

    val chainGw: BlockchainGateway by lazy {
        blockchainGatewayProvider.getGateway(getNetwork())
    }

    fun getBlockchainGateway(): BlockchainGateway {
        return chainGw
    }

    fun defaultPositionFetcher(address: String): PositionFetcher {
        return PositionFetcher(
            address,
            { user ->
                erC20Resource.balanceOfFunction(address, user, getNetwork())
            },
            { retVal ->
                val result = retVal[0].value as BigInteger
                Position(result, result)
            }
        )
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

    suspend fun getAbi(name: String): String {
        return abiResource.getABI(name)
    }

    override fun getProtocol(): Protocol {
        return protocolProvider.getProtocol()
    }
}