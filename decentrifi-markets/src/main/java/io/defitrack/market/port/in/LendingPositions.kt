package io.defitrack.market.port.`in`

import io.defitrack.market.domain.lending.LendingPosition

interface LendingPositions {
    suspend fun getPositions(protocol: String, address: String): List<LendingPosition>
}