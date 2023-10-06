package io.defitrack.claimables

import io.defitrack.claimable.ClaimableMarketVO
import io.defitrack.claimable.UserClaimableVO
import io.defitrack.protocol.ProtocolVO

interface ClaimablesClient {
    suspend fun getClaimables(address: String, protocol: ProtocolVO): List<UserClaimableVO>
    suspend fun getClaimableMarkets(protocol: ProtocolVO): List<ClaimableMarketVO>
}