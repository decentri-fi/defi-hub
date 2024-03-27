package io.defitrack.market.config

import io.defitrack.claim.AbstractClaimableMarketProvider
import io.defitrack.market.port.out.MarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.market.domain.lending.LendingMarket
import io.defitrack.market.domain.PoolingMarket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.availability.AvailabilityChangeEvent
import org.springframework.boot.availability.ReadinessState
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import kotlin.time.measureTime

@Configuration
class MarketCacheRefresher(
    private val poolingMarketProviders: List<MarketProvider<PoolingMarket>>,
    private val lendingMarketProviders: List<MarketProvider<LendingMarket>>,
    private val farmingMarketProviders: List<MarketProvider<FarmingMarket>>,
    private val claimableMarketProviders: List<AbstractClaimableMarketProvider>,
    private val applicationContext: ApplicationContext
) : ApplicationRunner {
    val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(
        fixedDelay = 1000 * 60 * 60 * 24 * 15, //every 15 days,
        initialDelay = 1000 * 60 * 60 * 24 * 15
    )
    fun populateCaches() {
        runBlocking(Dispatchers.Default) {
            if (poolingMarketProviders.isEmpty() && lendingMarketProviders.isEmpty() && farmingMarketProviders.isEmpty()) {
                AvailabilityChangeEvent.publish(applicationContext, ReadinessState.ACCEPTING_TRAFFIC)
                return@runBlocking
            }
            logger.info("Initial population of all caches.")
            val fullMeasurement = measureTime {
                poolingMarketProviders.sortedBy { it.order() }.forEach {
                    it.populateCaches()
                }
                lendingMarketProviders.sortedBy { it.order() }.forEach {
                    it.populateCaches()
                }
                farmingMarketProviders.sortedBy { it.order() }.forEach {
                    it.populateCaches()
                }
                claimableMarketProviders.forEach { it.populateCaches() }
            }
            logger.info("done with initial population of all caches. (took ${fullMeasurement.inWholeSeconds} seconds)")
        }
        AvailabilityChangeEvent.publish(applicationContext, ReadinessState.ACCEPTING_TRAFFIC)
    }

    @Scheduled(
        fixedDelay = 1000 * 60 * 60 * 1,
        initialDelay = 1000 * 60 * 60 * 1
    )
    fun refreshCaches() = runBlocking(Dispatchers.Default) {
        if (poolingMarketProviders.isEmpty() && lendingMarketProviders.isEmpty() && farmingMarketProviders.isEmpty()) {
            return@runBlocking
        }

        logger.info("Refreshing all caches.")
        poolingMarketProviders.map {
            it.refreshMarkets()
        }
        lendingMarketProviders.forEach {
            it.refreshMarkets()
        }
        farmingMarketProviders.forEach {
            it.refreshMarkets()
        }
        claimableMarketProviders.map {
            it.populateCaches()
        }
    }

    override fun run(args: ApplicationArguments?) {
        populateCaches()
    }
}