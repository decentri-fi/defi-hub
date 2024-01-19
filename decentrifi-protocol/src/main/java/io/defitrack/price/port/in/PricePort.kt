package io.defitrack.price.port.`in`

import io.defitrack.price.domain.GetPriceCommand


interface PricePort {
    suspend fun calculatePrice(getPriceCommand: GetPriceCommand): Double

}