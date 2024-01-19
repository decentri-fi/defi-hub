package io.defitrack.claimable.vo

import io.defitrack.domain.FungibleToken
import io.defitrack.domain.NetworkInformation
import io.defitrack.protocol.ProtocolVO

data class ClaimableMarketVO(
    val id: String,
    val name: String,
    val network: NetworkInformation,
    val protocol: ProtocolVO,
    val rewards: List<Reward>
) {
    data class Reward(
        val token: FungibleToken
    )
}