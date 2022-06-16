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
    fun init() {
        try {
            cache.invalidateAll()
            getPoolingMarkets()
        } catch (ex: Exception) {
            logger.error("something went wrong trying to populate the cache", ex)
        }
    }

    protected abstract suspend fun fetchPoolingMarkets(): List<PoolingMarketElement>

    fun getPoolingMarkets(): List<PoolingMarketElement> = runBlocking(Dispatchers.IO) {
        cache.get("all") {
            try {
                logger.info("Cache empty or expired, fetching fresh elements")
                val elements = fetchPoolingMarkets()
                logger.info("Cache successfuly filled with ${elements.size} elements")
                elements
            } catch (ex: Exception) {
                logger.error("Unable to fetch pooling markets: {}", ex.message)
                emptyList()
            }
        }
    }
}