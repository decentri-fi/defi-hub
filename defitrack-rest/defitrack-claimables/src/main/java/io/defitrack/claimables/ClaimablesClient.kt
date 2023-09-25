package io.defitrack.claimables

import io.defitrack.claimable.ClaimableVO
import io.defitrack.protocol.ProtocolVO

interface ClaimablesClient {
    suspend fun getClaimables(address: String, protocol: ProtocolVO): List<ClaimableVO>
}