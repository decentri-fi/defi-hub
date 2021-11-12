package io.codechef.defitrack.protocol.dmm.config

import io.codechef.defitrack.config.DefaultCacheConfig
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class CacheConfig : DefaultCacheConfig() {

    override fun caches(): List<CaffeineCache> {
        return listOf(
            buildCache("dmm-lps", 10, TimeUnit.MINUTES),
            buildCache("dmm-aprs", 1, TimeUnit.HOURS)
        )
    }
}