package io.defitrack.price.decentrifi

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled

@Configuration
class PriceUpdateCron(
    private val priceRepos: List<PriceRepository>
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 1)
    fun updatePrices() {
        logger.info("Updating prices from decentrifi")
        priceRepos.sortedBy {
            it.order()
        }.forEach {
            logger.info("Updating prices from ${it::class.simpleName}")
            it.populate()
        }
    }
}