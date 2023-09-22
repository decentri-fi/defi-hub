package io.defitrack.market.pooling

import io.defitrack.market.MarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.pooling.domain.PoolingMarket
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.availability.ApplicationAvailability
import org.springframework.boot.availability.AvailabilityChangeEvent
import org.springframework.boot.availability.ReadinessState
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Configuration
class MarketCacheRefresher(
    private val poolingMarketProviders: List<MarketProvider<PoolingMarket>>,
    private val lendingMarketProviders: List<MarketProvider<LendingMarket>>,
    private val farmingMarketProviders: List<MarketProvider<FarmingMarket>>,
    private val applicationContext: ApplicationContext
) : ApplicationRunner {
    val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(
        fixedDelay = 1000 * 60 * 60 * 24 * 3, //every 3 days,
        initialDelay = 1000 * 60 * 60 * 24 * 3
    )
    fun populateCaches() {
        runBlocking(Dispatchers.Default) {
            logger.info("Initial population of all caches.")
            poolingMarketProviders.map {
                launch {
                    it.populateCaches()
                }
            }.joinAll()
            lendingMarketProviders.forEach {
                it.populateCaches()
            }
            farmingMarketProviders.forEach {
                it.populateCaches()
            }
            logger.info("done with initial population of all caches.")
        }
        AvailabilityChangeEvent.publish(applicationContext, ReadinessState.ACCEPTING_TRAFFIC)
    }

    @Scheduled(
        fixedDelay = 1000 * 60 * 60 * 3,
        initialDelay = 1000 * 60 * 60 * 3
    ) //todo: make sure every one is just a refresh
    fun refreshCaches() = runBlocking(Dispatchers.Default) {
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

    override fun run(args: ApplicationArguments?) {
        populateCaches()
    }
}