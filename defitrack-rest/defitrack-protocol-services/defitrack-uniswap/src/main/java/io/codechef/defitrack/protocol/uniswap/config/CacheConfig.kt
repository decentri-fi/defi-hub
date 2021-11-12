package io.codechef.defitrack.protocol.uniswap.config

import io.codechef.defitrack.config.DefaultCacheConfig
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class CacheConfig : DefaultCacheConfig() {

    override fun caches(): List<CaffeineCache> {
        return listOf(
            buildCache("uniswap-pairs", 7, TimeUnit.DAYS),
            buildCache("uniswap-lps", 24, TimeUnit.HOURS),
            buildCache("uniswap-aprs", 24, TimeUnit.HOURS),
        )
    }
}