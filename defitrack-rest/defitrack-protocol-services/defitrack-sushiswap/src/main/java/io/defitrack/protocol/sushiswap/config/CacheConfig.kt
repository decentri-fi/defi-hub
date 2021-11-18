package io.defitrack.protocol.sushiswap.config

import io.defitrack.config.DefaultCacheConfig
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class CacheConfig : DefaultCacheConfig() {

    override fun caches(): List<CaffeineCache> {
        return listOf(
            buildCache("sushiswap-pairs", 7, TimeUnit.DAYS),
            buildCache("sushiswap-lps", 24, TimeUnit.HOURS),
            buildCache("sushiswap-aprs", 24, TimeUnit.HOURS),
        )
    }
}