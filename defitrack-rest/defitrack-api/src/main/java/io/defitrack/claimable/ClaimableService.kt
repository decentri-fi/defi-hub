package io.defitrack.claimable

import io.defitrack.protocol.ProtocolService

interface ClaimableService : ProtocolService {
    suspend fun claimables(address: String): List<Claimable> = emptyList()
}