package io.defitrack.port.input

import io.defitrack.domain.GetPriceCommand

interface PriceResource {
    suspend fun calculatePrice(getPriceCommand: GetPriceCommand): Double
}