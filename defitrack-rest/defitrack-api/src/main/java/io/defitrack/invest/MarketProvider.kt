package io.defitrack.invest

import io.defitrack.erc20.TokenInformationVO
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.ProtocolService
import io.defitrack.token.ERC20Resource
import io.defitrack.token.FungibleToken
import io.defitrack.token.MarketSizeService
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.time.Duration.Companion.hours

abstract class MarketProvider<T> : ProtocolService {

    val cache = Cache.Builder().expireAfterWrite(4.hours).build<String, List<T>>()
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    private lateinit var erC20Resource: ERC20Resource

    @Autowired
    lateinit var marketSizeService: MarketSizeService

    @Autowired
    private lateinit var blockchainGatewayProvider: BlockchainGatewayProvider

    protected abstract suspend fun fetchMarkets(): List<T>

    fun refreshCaches() = runBlocking(Dispatchers.Default) {
        try {
            val markets = populate()
            cache.put("all", markets)
            logger.info("Cache successfuly filled with ${markets.size} elements")
        } catch (ex: Exception) {
            logger.error("something went wrong trying to populate the cache", ex)
        }
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
                retVal[0].value as BigInteger
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

    fun getERC20Resource(): ERC20Resource {
        return erC20Resource
    }
}