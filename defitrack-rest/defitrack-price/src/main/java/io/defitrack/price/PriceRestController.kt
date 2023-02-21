package io.defitrack.price

import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/")
class PriceRestController(
    private val priceCalculator: PriceCalculator
) {

    @PostMapping
    fun calculatePrice(@RequestBody priceRequest: PriceRequest): Double = runBlocking {
        priceCalculator.calculatePrice(priceRequest)
    }
}