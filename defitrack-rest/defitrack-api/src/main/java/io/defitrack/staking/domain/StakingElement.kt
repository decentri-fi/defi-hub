package io.defitrack.staking.domain

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import java.math.BigInteger

data class StakingElement(
    val id: String,
    val network: Network,
    val user: String = "",
    val protocol: Protocol,
    val name: String = "unknown",
    val contractAddress: String,
    val vaultType: String,
    val rate: Double = 0.0,
    val url: String = "?",
    val stakedToken: StakedToken,
    val amount: BigInteger,
    val rewardTokens: List<RewardToken> = emptyList()
)
