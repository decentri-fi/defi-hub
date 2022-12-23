package io.defitrack.market.pooling

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled

@Configuration
class PoolingMarketCacheRefresher(private val poolingMarketProvider: List<PoolingMarketProvider>) {

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 3)
    fun refreshCaches() {
        poolingMarketProvider.forEach {
            it.refreshCaches()
        }
    }
}