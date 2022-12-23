package io.defitrack.claimable

import io.defitrack.protocol.ProtocolService

interface ClaimableRewardProvider : ProtocolService {
    suspend fun claimables(address: String): List<Claimable> = emptyList()
}