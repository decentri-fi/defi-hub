package io.defitrack.claimables.ports.outputs

import io.defitrack.claimable.vo.ClaimableMarketVO
import io.defitrack.claimable.vo.UserClaimableVO
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.ProtocolVO

interface ClaimablesClient {
    suspend fun getClaimables(address: String, protocols: List<Protocol>): List<UserClaimableVO>
    suspend fun getClaimableMarkets(protocol: ProtocolVO): List<ClaimableMarketVO>
}