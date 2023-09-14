package io.defitrack.price

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class PriceRestController(
    private val priceCalculator: PriceCalculator
) {
    @PostMapping
    suspend fun calculatePrice(@RequestBody priceRequest: PriceRequest): Double {
        return priceCalculator.calculatePrice(priceRequest)
    }
}