package io.defitrack.protocol.beefy.config

import io.defitrack.config.DefaultCacheConfig
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class CacheConfig : DefaultCacheConfig() {

    override fun caches(): List<CaffeineCache> {
        return listOf(
            buildCache("beefy-api-apys", 1, TimeUnit.HOURS),
        )
    }
}