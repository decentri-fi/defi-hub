package io.codechef.defitrack.config

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Ticker
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
@EnableCaching
abstract class DefaultCacheConfig {
    abstract fun caches(): List<CaffeineCache>

    @Bean
    fun cacheManager(): CacheManager {
        val manager = SimpleCacheManager()
        manager.setCaches(
            caches() + listOf(
                buildCache("prices", 1, TimeUnit.HOURS),
                buildCache("abis", 7, TimeUnit.DAYS),
                buildCache("tokens", 7, TimeUnit.DAYS)
            )
        )
        return manager;
    }

    fun buildCache(name: String, ttl: Int, ttlUnit: TimeUnit): CaffeineCache {
        return CaffeineCache(
            name, Caffeine.newBuilder()
                .expireAfterWrite(ttl.toLong(), ttlUnit)
                .ticker(Ticker.systemTicker())
                .build()
        )
    }
}