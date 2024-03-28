package io.defitrack.claimable.adapter.`in`.rest.domain

import io.defitrack.erc20.FungibleTokenInformationVO
import io.defitrack.network.NetworkInformationVO
import io.defitrack.protocol.ProtocolVO

data class ClaimableMarketVO(
    val id: String,
    val name: String,
    val network: NetworkInformationVO,
    val protocol: ProtocolVO,
    val rewards: List<Reward>
) {
    data class Reward(
        val token: FungibleTokenInformationVO
    )
}