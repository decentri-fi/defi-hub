package io.defitrack.price.port.out

import io.defitrack.price.domain.GetPriceCommand

interface Prices {
    suspend fun calculatePrice(getPriceCommand: GetPriceCommand): Double
}