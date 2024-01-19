package io.defitrack.price

import io.defitrack.common.network.Network
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.price.external.ExternalPrice
import io.defitrack.price.external.ExternalPriceService
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/")
class PriceRestController(
    private val priceCalculator: PriceCalculator,
    private val externalPriceServices: List<ExternalPriceService>,
    private val observationRegistry: ObservationRegistry
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping
    suspend fun calculatePrice(@RequestBody getPriceCommand: GetPriceCommand): Double {
        val observation = Observation.start("price-calculate", observationRegistry)
        return observation.openScope().use {
            priceCalculator.calculatePrice(getPriceCommand)
        }.also {
            observation.stop()
        }
    }

    @GetMapping
    suspend fun getAllPrices(): List<ExternalPrice> {
        return externalPriceServices.flatMap {
            it.getAllPrices()
        }
    }


    @GetMapping("/{address}")
    suspend fun getPricePerToken(
        @PathVariable("address") address: String,
        @RequestParam("network") networkName: String
    ): Map<String, Any> {

        val network = Network.fromString(networkName) ?: return mapOf(
            "price" to BigDecimal.ZERO
        )

        try {
            return mapOf(
                "price" to priceCalculator.calculatePrice(
                    GetPriceCommand(
                        address,
                        network,
                        BigDecimal.ONE
                    )
                )
            )
        } catch (ex: Exception) {
            logger.error("Error calculating price for $address on $networkName", ex)
            return mapOf(
                "price" to BigDecimal.ZERO
            )
        }
    }
}