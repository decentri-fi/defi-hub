package io.defitrack.market.pooling

import io.defitrack.market.MarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.pooling.domain.PoolingMarket
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled

@Configuration
class MarketCacheRefresher(
    private val poolingMarketProviders: List<MarketProvider<PoolingMarket>>,
    private val lendingMarketProviders: List<MarketProvider<LendingMarket>>,
    private val farmingMarketProviders: List<MarketProvider<FarmingMarket>>,
) {
    val logger = LoggerFactory.getLogger(this::class.java)

    @PostConstruct
    fun populateCaches() {
        logger.info("Initial population of all cashes.")
        poolingMarketProviders.forEach {
            it.populateCaches()
        }
        lendingMarketProviders.forEach {
            it.populateCaches()
        }
        farmingMarketProviders.forEach {
            it.populateCaches()
        }
    }

    @Scheduled(
        fixedDelay = 1000 * 60 * 60 * 3,
        initialDelay = 1000 * 60 * 60 * 3
    ) //todo: make sure every one is just a refresh
    fun refreshCaches() {
        logger.info("Refreshing all caches. No full population.")
        poolingMarketProviders.forEach {
            it.populateCaches()
        }
        lendingMarketProviders.forEach {
            it.refreshMarkets()
        }
        farmingMarketProviders.forEach {
            it.populateCaches()
        }
    }
}