package io.defitrack.claimable.vo

import io.defitrack.token.FungibleToken
import io.defitrack.network.NetworkInformation
import io.defitrack.protocol.ProtocolInformation

data class ClaimableMarketVO(
    val id: String,
    val name: String,
    val network: NetworkInformation,
    val protocol: ProtocolInformation,
    val rewards: List<Reward>
) {
    data class Reward(
        val token: FungibleToken
    )
}