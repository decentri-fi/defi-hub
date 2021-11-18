package io.defitrack.config

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Ticker
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class CacheConfig {

    @Bean
    fun cacheManager(): CacheManager {
        val manager = SimpleCacheManager()
        manager.setCaches(
            listOf(
                buildCache("beefy-api-prices", 1, TimeUnit.DAYS),
                buildCache("external-prices", 15, TimeUnit.MINUTES),
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