package io.defitrack.lending

import io.defitrack.lending.domain.LendingMarketElement
import io.defitrack.protocol.ProtocolService
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.Executors
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

abstract class LendingMarketService : ProtocolService {

    val logger = LoggerFactory.getLogger(this.javaClass)

    @OptIn(ExperimentalTime::class)
    val cache = Cache.Builder().expireAfterWrite(
        Duration.Companion.hours(4)
    ).build<String, List<LendingMarketElement>>()

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 3)
    fun init() {
        try {
            cache.invalidateAll()
            Executors.newSingleThreadExecutor().submit {
                getLendingMarkets()
            }
        } catch (ex: Exception) {
            logger.error("something went wrong trying to populate the cache", ex)
        }
    }

    abstract fun fetchLendingMarkets(): List<LendingMarketElement>

    fun getLendingMarkets(): List<LendingMarketElement> = runBlocking(Dispatchers.IO) {
        cache.get("all") {
            logger.info("Cache empty or expired, fetching fresh elements")
            val elements = fetchLendingMarkets()
            logger.info("Cache successfuly filled with ${elements.size} elements")
            elements
        }
    }
}