package io.defitrack.market.pooling

import io.defitrack.market.MarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.pooling.domain.PoolingMarket
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

    @Scheduled(
        fixedDelay = 1000 * 60 * 60 * 24 * 3, //every 3 days
    )
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
    fun refreshCaches() = runBlocking {
        logger.info("Refreshing all caches. No full population.")
        poolingMarketProviders.map {
            launch {
                it.refreshMarkets()
            }
        }.joinAll()

        lendingMarketProviders.map {
            launch {
                it.refreshMarkets()
            }
        }.joinAll()
        farmingMarketProviders.map {
            launch {
                it.refreshMarkets()
            }
        }.joinAll()
    }
}