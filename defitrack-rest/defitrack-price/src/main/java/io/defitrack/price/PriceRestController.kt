package io.defitrack.price

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/")
class PriceRestController(
    private val priceCalculator: PriceCalculator,
    private val priceProvider: PriceProvider
) {

    @PostMapping
    fun calculatePrice(@RequestBody priceRequest: PriceRequest): Double {
        return priceCalculator.calculatePrice(priceRequest)
    }
}