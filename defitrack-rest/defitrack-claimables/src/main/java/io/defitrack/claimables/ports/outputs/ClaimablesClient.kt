package io.defitrack.claimables.ports.outputs

import io.defitrack.claimable.vo.ClaimableMarketVO
import io.defitrack.claimable.vo.UserClaimableVO
import io.defitrack.protocol.Protocol

interface ClaimablesClient {
    suspend fun getClaimables(address: String, protocols: List<Protocol>): List<UserClaimableVO>
    suspend fun getClaimableMarkets(protocol: Protocol): List<ClaimableMarketVO>
}