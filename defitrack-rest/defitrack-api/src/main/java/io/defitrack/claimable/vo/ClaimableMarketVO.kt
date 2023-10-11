package io.defitrack.claimable.vo

import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import io.defitrack.token.FungibleToken

data class ClaimableMarketVO(
    val id: String,
    val name: String,
    val network: NetworkVO,
    val protocol: ProtocolVO,
    val rewards: List<Reward>
) {
    data class Reward(
        val token: FungibleToken
    )
}