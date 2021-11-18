package io.defitrack.protocol.adamant.config

import io.codechef.defitrack.config.DefaultCacheConfig
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.context.annotation.Configuration

@Configuration
class CacheConfig : DefaultCacheConfig() {

    override fun caches(): List<CaffeineCache> {
        return listOf(

        )
    }
}