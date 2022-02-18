package io.defitrack.staking.vo

import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import java.math.BigInteger

data class StakingElementVO(
    val id: String,
    val network: NetworkVO,
    val protocol: ProtocolVO,
    val dollarValue: Double,
    val name: String,
    val contractAddress: String,
    val vaultType: String,
    val rate: Double?,
    val stakedToken: StakedTokenVO,
    val amount: BigInteger,
    val rewardTokens: List<RewardTokenVO>
)