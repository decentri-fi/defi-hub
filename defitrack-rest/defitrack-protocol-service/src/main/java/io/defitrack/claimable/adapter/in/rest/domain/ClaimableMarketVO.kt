package io.defitrack.claimable.adapter.`in`.rest.domain

import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.networkinfo.NetworkInformation
import io.defitrack.protocol.ProtocolVO

//TODO: move
data class ClaimableMarketVO(
    val id: String,
    val name: String,
    val network: NetworkInformation,
    val protocol: ProtocolVO,
    val rewards: List<Reward>
) {
    data class Reward(
        val token: FungibleTokenInformation
    )
}