package io.defitrack.staking.domain

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.staking.TokenType
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
    val stakedToken: VaultStakedToken? = null,
    val rewardTokens: List<VaultRewardToken> = emptyList()
)

class VaultStakedToken(
    val address: String,
    val network: Network,
    val amount: BigInteger,
    val symbol: String,
    val name: String,
    val decimals: Int,
    val type: TokenType
)

data class VaultRewardToken(
    var daily: String = "",
    var name: String = "",
    var symbol: String = "",
    var url: String = "",
    var decimals: Int = 18
)