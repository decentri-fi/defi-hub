package io.defitrack.market.port.`in`

import io.defitrack.market.domain.farming.FarmingPosition

interface FarmingPositions {
    suspend fun getPositions(
         protocol: String,
         address: String
    ): List<FarmingPosition>
}