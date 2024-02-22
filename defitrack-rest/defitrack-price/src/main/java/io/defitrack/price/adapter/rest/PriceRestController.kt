package io.defitrack.price.adapter.rest

import io.defitrack.common.network.Network
import io.defitrack.price.application.DefaultPriceCalculator
import io.defitrack.price.application.PriceAggregator
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.price.external.domain.ExternalPrice
import io.defitrack.price.port.`in`.PriceCalculator
import io.defitrack.price.port.out.ExternalPriceService
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/")
class PriceRestController(
    private val priceCalculator: PriceCalculator,
    private val priceAggregator: PriceAggregator
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping
    suspend fun calculatePrice(@RequestBody getPriceCommand: GetPriceCommand): Double {
        return priceCalculator.calculatePrice(getPriceCommand)
    }

    @GetMapping
    suspend fun getAllPrices(
        @RequestParam("network", required = false) networkName: String? = null,
        @RequestParam("type", required = false) type: String? = null,
    ): List<ExternalPrice> {
        return priceAggregator.getAllPrices()
            .filter {
                networkName == null || networkName.lowercase() == it.network.slug
            }.filter {
                type == null || type.lowercase() == it.source
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