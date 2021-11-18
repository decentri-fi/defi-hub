package io.defitrack.claimable

import io.defitrack.protocol.ProtocolService

interface ClaimableService : ProtocolService {
    fun claimables(address: String): List<ClaimableElement> = emptyList()
}