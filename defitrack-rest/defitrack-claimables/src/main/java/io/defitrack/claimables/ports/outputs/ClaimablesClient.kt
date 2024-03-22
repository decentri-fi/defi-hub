package io.defitrack.claimables.ports.outputs

import io.defitrack.claimable.vo.ClaimableMarketVO
import io.defitrack.claimables.domain.UserClaimableDTO
import io.defitrack.protocol.Protocol

interface ClaimablesClient {
    suspend fun getClaimables(address: String, protocols: List<Protocol>): List<UserClaimableDTO>
    suspend fun getClaimableMarkets(protocol: Protocol): List<ClaimableMarketVO>
}