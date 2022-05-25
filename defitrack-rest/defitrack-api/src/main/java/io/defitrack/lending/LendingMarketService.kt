package io.defitrack.lending

import io.defitrack.lending.domain.LendingMarket
import io.defitrack.protocol.ProtocolService
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.hours

abstract class LendingMarketService : ProtocolService {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    val cache = Cache.Builder().expireAfterWrite(4.hours).build<String, List<LendingMarket>>()

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 3)
    fun init() {
        try {
            cache.invalidateAll()
            val result = Executors.newSingleThreadExecutor().submit {
                getLendingMarkets()
            }
            result.get()
        } catch (ex: Exception) {
            logger.error("something went wrong trying to populate the cache", ex)
        }
    }

    abstract suspend fun fetchLendingMarkets(): List<LendingMarket>

    fun getLendingMarkets(): List<LendingMarket> = runBlocking(Dispatchers.IO) {
        cache.get("all") {
            logger.info("Cache empty or expired, fetching fresh elements")
            fetchLendingMarkets().also {
                logger.info("Cache successfuly filled with ${it.size} elements")
            }
        }
    }
}