package io.defitrack.market.lending

import io.defitrack.market.lending.domain.LendingPosition
import io.defitrack.protocol.ProtocolService
import kotlinx.coroutines.runBlocking

interface LendingPositionService : ProtocolService {
    suspend fun getLendings(address: String): List<LendingPosition>

    fun getLending(address: String, marketId: String): LendingPosition? = runBlocking {
        getLendings(address).firstOrNull {
            it.market.id == marketId
        }
    }
}