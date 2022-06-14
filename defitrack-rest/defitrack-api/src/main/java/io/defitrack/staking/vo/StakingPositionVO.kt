package io.defitrack.staking.vo

import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import io.defitrack.token.FungibleToken

data class StakingPositionVO(
    val id: String,
    val network: NetworkVO,
    val protocol: ProtocolVO,
    val dollarValue: Double,
    val name: String,
    val contractAddress: String,
    val vaultType: String,
    val apr: Double?,
    val stakedToken: FungibleToken,
    val amount: Double,
    val underlyingAmount: Double?,
    val rewardTokens: List<FungibleToken>
)