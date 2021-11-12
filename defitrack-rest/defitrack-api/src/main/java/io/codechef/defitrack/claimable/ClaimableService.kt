package io.codechef.defitrack.claimable

import io.codechef.defitrack.protocol.ProtocolService

interface ClaimableService : ProtocolService {
    fun claimables(address: String): List<ClaimableElement> = emptyList()
}