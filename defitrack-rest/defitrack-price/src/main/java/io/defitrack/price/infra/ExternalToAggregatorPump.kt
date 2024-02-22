package io.defitrack.price.infra

import io.defitrack.price.application.PriceAggregator
import io.defitrack.price.external.domain.ExternalPrice
import io.defitrack.price.port.out.ExternalPriceService
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ExternalToAggregatorPump(
    private val externalPriceServices: List<ExternalPriceService>,
    private val priceAggregator: PriceAggregator
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(fixedRate = 1000 * 60 * 60) //every hour
    fun init() = runBlocking {
        logger.info("found ${externalPriceServices.size} external price services")
        externalPriceServices.sortedBy {
            it.order()
        }.forEach {
            val allPrices = it.getAllPrices()
            logger.info("found ${allPrices.size} prices from ${it.javaClass.simpleName}")
            allPrices.forEach { externalPrice ->
                priceAggregator.addPrice(externalPrice)
            }
        }
        logger.info("External prices fetched with ${priceAggregator.getAllPrices().size} prices.")
    }
}