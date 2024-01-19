package io.defitrack.port.output

import io.defitrack.domain.GetPriceCommand

internal interface Prices {
    suspend fun calculatePrice(getPriceCommand: GetPriceCommand): Double
}