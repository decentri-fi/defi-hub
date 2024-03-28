package io.defitrack.port.output

import io.defitrack.adapter.output.domain.market.GetPriceCommand

interface PriceClient {

    suspend fun calculatePrice(getPriceCommand: GetPriceCommand): Double
}
