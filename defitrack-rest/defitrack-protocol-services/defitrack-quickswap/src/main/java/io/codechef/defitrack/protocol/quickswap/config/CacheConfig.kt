package io.codechef.defitrack.protocol.quickswap.config

import io.codechef.defitrack.config.DefaultCacheConfig
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class CacheConfig : DefaultCacheConfig() {

    override fun caches(): List<CaffeineCache> {
        return listOf(
            buildCache("quickswap-pairs", 7, TimeUnit.DAYS),
            buildCache("quickswap-lps", 24, TimeUnit.HOURS),
            buildCache("quickswap-aprs", 24, TimeUnit.HOURS),
            buildCache("quickswap-vaults", 1, TimeUnit.DAYS),
        )
    }
}