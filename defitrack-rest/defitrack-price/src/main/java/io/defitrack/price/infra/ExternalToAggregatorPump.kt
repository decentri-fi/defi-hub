package io.defitrack.price.infra

import arrow.core.Either.Companion.catch
import io.defitrack.price.application.PriceAggregator
import io.defitrack.price.port.out.ExternalPriceService
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.time.measureTime

@Component
class ExternalToAggregatorPump(
    private val externalPriceServices: List<ExternalPriceService>,
    private val priceAggregator: PriceAggregator
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(fixedRate = 1000 * 60 * 60) //every hour
    fun init() = runBlocking {
        logger.debug("found ${externalPriceServices.size} external price services")
        externalPriceServices
            .sortedBy(ExternalPriceService::importOrder)
            .forEach {
                catch {
                    logger.debug("collecting prices from ${it.javaClass.simpleName}")
                    val time = measureTime {
                        val allPrices = it.getAllPrices()
                        allPrices.collect { externalPrice ->
                            priceAggregator.addPrice(externalPrice)
                        }
                    }
                    logger.info("Fetched prices from ${it.javaClass.simpleName} in ${time.inWholeSeconds}s")
                }.mapLeft {
                    logger.error("Error fetching prices from ${it.javaClass.simpleName}", it)
                }
            }
        logger.info("External prices fetched with ${priceAggregator.getAllPrices().size} prices.")
    }
}