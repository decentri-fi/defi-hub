package io.defitrack.price

import io.defitrack.common.network.Network
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
    private val priceCalculator: PriceCalculator
) {
    @PostMapping
    suspend fun calculatePrice(@RequestBody priceRequest: PriceRequest): Double {
        return priceCalculator.calculatePrice(priceRequest)
    }


    @GetMapping("/{address}")
    suspend fun getPricePerToken(
        @PathVariable("address") address: String,
        @RequestParam("network") networkName: String
    ): Map<String, Any> {

        val network = Network.fromString(networkName) ?: return mapOf(
            "price" to BigDecimal.ZERO
        )

        return mapOf(
            "price" to priceCalculator.calculatePrice(
                PriceRequest(
                    address,
                    network,
                    BigDecimal.ONE
                )
            )
        )
    }
}