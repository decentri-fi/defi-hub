package io.defitrack.staking.vo

import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO

data class StakingElementVO(
    val id: String,
    val network: NetworkVO,
    val protocol: ProtocolVO,
    val dollarValue: Double,
    val name: String,
    val contractAddress: String,
    val vaultType: String,
    val rate: Double = 0.0,
    val url: String,
    val stakedToken: StakedTokenVO,
    val rewardTokens: List<RewardTokenVO>
)