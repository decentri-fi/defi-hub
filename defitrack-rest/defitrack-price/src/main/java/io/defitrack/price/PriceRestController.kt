package io.defitrack.price

import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/")
class PriceRestController(
    private val priceCalculator: PriceCalculator,
    private val priceProvider: PriceProvider
) {

    @GetMapping("/{tokenName}")
    fun getPriceByName(@PathVariable("tokenName") tokenName: String): BigDecimal {
        return priceProvider.getPrice(tokenName)
    }

    @PostMapping
    fun calculatePrice(@RequestBody priceRequest: PriceRequest): Double {
        return priceCalculator.calculatePrice(priceRequest)
    }
}