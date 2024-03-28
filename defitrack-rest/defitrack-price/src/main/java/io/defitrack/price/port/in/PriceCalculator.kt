package io.defitrack.price.port.`in`

import io.defitrack.adapter.output.domain.market.GetPriceCommand


interface PriceCalculator {
    suspend fun calculatePrice(getPriceCommand: GetPriceCommand): Double
}