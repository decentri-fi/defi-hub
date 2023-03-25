package io.defitrack.market.pooling

import io.defitrack.market.MarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.pooling.domain.PoolingMarket
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled

@Configuration
class MarketCacheRefresher(
    private val poolingMarketProviders: List<MarketProvider<PoolingMarket>>,
    private val lendingMarketProviders: List<MarketProvider<LendingMarket>>,
    private val farmingMarketProviders: List<MarketProvider<FarmingMarket>>,
) {

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 3)
    fun refreshCaches() {
        poolingMarketProviders.forEach {
            it.refreshCaches()
        }
        lendingMarketProviders.forEach {
            it.refreshCaches()
        }
        farmingMarketProviders.forEach {
            it.refreshCaches()
        }
    }
}