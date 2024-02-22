package io.defitrack.price.port.`in`

import io.defitrack.price.domain.GetPriceCommand

interface PriceCalculator {
    suspend fun calculatePrice(getPriceCommand: GetPriceCommand): Double
}