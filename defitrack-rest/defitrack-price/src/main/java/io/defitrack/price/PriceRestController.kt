package io.defitrack.price

import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/")
class PriceRestController(private val priceCalculator: PriceCalculator) {

    @GetMapping("/{tokenName}")
    fun getPriceByName(@PathVariable("tokenName") tokenName: String): BigDecimal {
        return priceCalculator.getPrice(tokenName)
    }

    @PostMapping
    fun calculatePrice(@RequestBody priceRequest: PriceRequest): Double {
        return priceCalculator.calculatePrice(priceRequest)
    }
}