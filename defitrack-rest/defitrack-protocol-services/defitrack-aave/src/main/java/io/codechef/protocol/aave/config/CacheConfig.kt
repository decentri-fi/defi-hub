package io.codechef.protocol.aave.config

import io.codechef.defitrack.config.DefaultCacheConfig
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class CacheConfig : DefaultCacheConfig() {

    override fun caches(): List<CaffeineCache> {
        return listOf(
            buildCache("aave-mainnet-reserves", 7, TimeUnit.DAYS),
            buildCache("aave-polygon-reserves", 7, TimeUnit.DAYS),
        )
    }
}