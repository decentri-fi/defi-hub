package io.codechef.price

import io.codechef.defitrack.price.ExternalPriceService
import io.codechef.defitrack.price.PriceRequest
import io.codechef.defitrack.price.PriceService
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/")
class PriceRestController(
    private val beefyPricesService: BeefyPricesService,
    private val priceService: PriceService,
    private val externalPriceServices: List<ExternalPriceService>
) {

    @GetMapping
    fun getPrices(): Map<String, BigDecimal> {
        return beefyPricesService.getPrices() + externalPriceServices.associate {
            it.getOracleName().uppercase() to it.getPrice()
        }
    }

    @GetMapping("/{tokenName}")
    fun getPriceByName(@PathVariable("tokenName") tokenName: String): BigDecimal {
        return getPrices()[tokenName] ?: BigDecimal.ZERO
    }

    @PostMapping
    fun calculatePrice(@RequestBody priceRequest: PriceRequest): Double {
        return priceService.calculatePrice(priceRequest)
    }
}