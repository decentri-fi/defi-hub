package io.defitrack.pool

import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.protocol.ProtocolService
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

abstract class PoolingMarketService : ProtocolService {

    @OptIn(ExperimentalTime::class)
    val cache = Cache.Builder().expireAfterWrite(
        Duration.Companion.hours(4)
    ).build<String, List<PoolingMarketElement>>()

    @PostConstruct
    @Scheduled(fixedDelay = 1000 * 60 * 60 * 3)
    fun init() {
        Executors.newSingleThreadExecutor().submit {
            getPoolingMarkets()
        }
    }

    abstract fun fetchPoolingMarkets(): List<PoolingMarketElement>

    fun getPoolingMarkets(): List<PoolingMarketElement> = runBlocking(Dispatchers.IO) {
        cache.get("all") {
            fetchPoolingMarkets()
        }
    }
}