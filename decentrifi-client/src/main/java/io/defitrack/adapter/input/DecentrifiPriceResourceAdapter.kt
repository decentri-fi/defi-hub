package io.defitrack.adapter.input

import io.defitrack.domain.GetPriceCommand
import io.defitrack.port.input.PriceResource
import io.defitrack.port.output.Prices
import org.springframework.stereotype.Component

@Component
internal class DecentrifiPriceResourceAdapter(
    private val prices: Prices
) : PriceResource{
    override suspend fun calculatePrice(getPriceCommand: GetPriceCommand): Double {
        return prices.calculatePrice(getPriceCommand)
    }
}