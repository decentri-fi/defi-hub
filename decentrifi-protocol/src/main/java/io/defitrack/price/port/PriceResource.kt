package io.defitrack.price.port

import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.price.port.`in`.PricePort
import io.defitrack.price.port.out.Prices
import org.springframework.stereotype.Component

@Component
class PriceResource(
    private val prices: Prices
) : PricePort {

    override suspend fun calculatePrice(getPriceCommand: GetPriceCommand): Double {
        return prices.calculatePrice(getPriceCommand)
    }
}