package io.defitrack.pool

import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.protocol.ProtocolService
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

abstract class PoolingMarketService : ProtocolService {

    val logger = LoggerFactory.getLogger(this.javaClass)

    @OptIn(ExperimentalTime::class)
    val cache = Cache.Builder().expireAfterWrite(
        Duration.Companion.hours(4)
    ).build<String, List<PoolingMarketElement>>()

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 3)
    fun init() {
        try {
            cache.invalidateAll()
            getPoolingMarkets()
        } catch (ex: Exception) {
            logger.error("something went wrong trying to populate the cache", ex)
        }
    }

    abstract suspend fun fetchPoolingMarkets(): List<PoolingMarketElement>

    fun getPoolingMarkets(): List<PoolingMarketElement> = runBlocking(Dispatchers.IO) {
        cache.get("all") {
            logger.info("Cache empty or expired, fetching fresh elements")
            val elements = fetchPoolingMarkets()
            logger.info("Cache successfuly filled with ${elements.size} elements")
            elements
        }
    }
}