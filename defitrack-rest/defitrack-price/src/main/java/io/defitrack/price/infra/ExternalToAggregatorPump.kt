package io.defitrack.price.infra

import arrow.core.Either
import arrow.core.Either.Companion.catch
import io.defitrack.price.application.PriceAggregator
import io.defitrack.price.external.domain.ExternalPrice
import io.defitrack.price.port.out.ExternalPriceService
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.flow.collect
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
        externalPriceServices
            .sortedBy(ExternalPriceService::order)
            .forEach {
                catch {
                    val allPrices = it.getAllPrices()
                    logger.info("collecting prices from ${it.javaClass.simpleName}")
                    allPrices.collect { externalPrice ->
                        priceAggregator.addPrice(externalPrice)
                    }
                }.mapLeft {
                    logger.error("Error fetching prices from ${it.javaClass.simpleName}", it)
                }
            }
        logger.info("External prices fetched with ${priceAggregator.getAllPrices().size} prices.")
    }
}