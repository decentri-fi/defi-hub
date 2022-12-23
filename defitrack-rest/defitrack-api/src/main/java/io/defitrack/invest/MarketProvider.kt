package io.defitrack.invest

import io.defitrack.market.DefiMarket
import io.defitrack.protocol.ProtocolService
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.hours

abstract class MarketProvider<T> : ProtocolService {

    val cache = Cache.Builder().expireAfterWrite(4.hours).build<String, List<T>>()
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

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
        logger.error("Unable to fetch pooling markets: {}", ex.message)
        emptyList()
    }

    fun getMarkets(): List<T> = runBlocking(Dispatchers.Default) {
        cache.get("all") ?: emptyList()
    }
}