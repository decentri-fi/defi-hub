package io.defitrack.claimables

import io.defitrack.claimable.vo.ClaimableMarketVO
import io.defitrack.claimable.vo.UserClaimableVO
import io.defitrack.protocol.ProtocolVO

interface ClaimablesClient {
    suspend fun getClaimables(address: String, protocol: ProtocolVO): List<UserClaimableVO>
    suspend fun getClaimableMarkets(protocol: ProtocolVO): List<ClaimableMarketVO>
}