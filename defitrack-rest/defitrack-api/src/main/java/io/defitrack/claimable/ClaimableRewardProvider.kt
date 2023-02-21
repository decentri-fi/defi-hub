package io.defitrack.claimable

import io.defitrack.protocol.ProtocolService

abstract class ClaimableRewardProvider : ProtocolService {
    abstract suspend fun claimables(address: String): List<Claimable>
}