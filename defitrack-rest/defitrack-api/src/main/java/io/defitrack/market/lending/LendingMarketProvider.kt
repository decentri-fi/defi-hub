package io.defitrack.market.lending

import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.protocol.ProtocolService
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.hours

abstract class LendingMarketProvider : ProtocolService {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    val cache = Cache.Builder().expireAfterWrite(4.hours).build<String, List<LendingMarket>>()

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 3)
    fun init() = runBlocking(Dispatchers.IO){
        withContext(Dispatchers.IO.limitedParallelism(5)) {
            try {
                val markets = populate()
                cache.invalidateAll()
                cache.put("all", markets)
            } catch (ex: Exception) {
                logger.error("something went wrong trying to populate the cache", ex)
            }
        }
    }

    abstract suspend fun fetchLendingMarkets(): List<LendingMarket>

    fun getLendingMarkets(): List<LendingMarket> = runBlocking(Dispatchers.Default) {
        cache.get("all") ?: emptyList()
    }

    private suspend fun populate(): List<LendingMarket> {
        logger.info("Cache empty or expired, fetching fresh elements")
        return fetchLendingMarkets().also {
            logger.info("Cache successfuly filled with ${it.size} elements")
        }
    }
}