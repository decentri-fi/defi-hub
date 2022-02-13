package io.defitrack.staking.domain

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenType
import java.math.BigDecimal


data class StakingMarketElement(
    val id: String,
    val network: Network,
    val protocol: Protocol,
    val name: String,
    val token: StakedToken,
    val reward: List<RewardToken>,
    val contractAddress: String,
    val vaultType: String,
    val marketSize: BigDecimal = BigDecimal.ZERO,
    val rate: BigDecimal = BigDecimal.ZERO
)

data class StakedToken(
    val name: String,
    val symbol: String,
    val address: String,
    val network: Network,
    val decimals: Int,
    val type: TokenType
)

data class RewardToken(
    var name: String = "",
    var symbol: String = "",
    var decimals: Int = 18
)