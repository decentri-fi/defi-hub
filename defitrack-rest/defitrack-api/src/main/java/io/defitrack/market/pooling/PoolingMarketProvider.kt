package io.defitrack.market.pooling

import io.defitrack.market.pooling.domain.PoolingMarketElement
import io.defitrack.protocol.ProtocolService
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import kotlin.time.Duration.Companion.hours

abstract class PoolingMarketProvider : ProtocolService {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    val cache = Cache.Builder().expireAfterWrite(4.hours).build<String, List<PoolingMarketElement>>()

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 3)
    fun init() = runBlocking {
        try {
            logger.info("Cache expired, fetching fresh elements")
            val markets = populate()
            cache.invalidateAll()
            cache.put("all", markets)
            logger.info("Cache successfuly filled with ${markets.size} elements")
        } catch (ex: Exception) {
            logger.error("something went wrong trying to populate the cache", ex)
        }
    }

    protected abstract suspend fun fetchPoolingMarkets(): List<PoolingMarketElement>

    fun getPoolingMarkets(): List<PoolingMarketElement> = runBlocking(Dispatchers.IO) {
        cache.get("all") ?: emptyList()
    }

    private suspend fun populate() = try {
        fetchPoolingMarkets()
    } catch (ex: Exception) {
        logger.error("Unable to fetch pooling markets: {}", ex.message)
        emptyList()
    }
}