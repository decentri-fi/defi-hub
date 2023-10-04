package io.defitrack.price

import io.defitrack.common.network.Network
import io.defitrack.price.external.ExternalPrice
import io.defitrack.price.external.ExternalPriceService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/")
class PriceRestController(
    private val priceCalculator: PriceCalculator,
    private val externalPriceServices: List<ExternalPriceService>
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping
    suspend fun calculatePrice(@RequestBody priceRequest: PriceRequest): Double {
        return priceCalculator.calculatePrice(priceRequest)
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
                    PriceRequest(
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