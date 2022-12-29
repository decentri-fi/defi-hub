package io.defitrack.invest

import io.defitrack.erc20.TokenInformationVO
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.DefiMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.ProtocolService
import io.defitrack.token.ERC20Resource
import io.defitrack.token.MarketSizeService
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigInteger
import kotlin.time.Duration.Companion.hours

abstract class MarketProvider<T> : ProtocolService {

    val cache = Cache.Builder().expireAfterWrite(4.hours).build<String, List<T>>()
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    lateinit var erC20Resource: ERC20Resource
    @Autowired
    lateinit var marketSizeService: MarketSizeService
    @Autowired
    lateinit var blockchainGatewayProvider: BlockchainGatewayProvider

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

    fun getBlockchainGateway(): BlockchainGateway {
        return blockchainGatewayProvider.getGateway(getNetwork())
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
}