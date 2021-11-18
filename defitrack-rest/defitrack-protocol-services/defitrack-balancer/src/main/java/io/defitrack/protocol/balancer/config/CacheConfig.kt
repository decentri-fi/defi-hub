package io.defitrack.protocol.balancer.config

import io.codechef.defitrack.config.DefaultCacheConfig
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class CacheConfig : DefaultCacheConfig() {

    override fun caches(): List<CaffeineCache> {
        return listOf(
            buildCache("balancer-lps", 10, TimeUnit.MINUTES),
            buildCache("balancer-markets", 1, TimeUnit.HOURS)
        )
    }
}