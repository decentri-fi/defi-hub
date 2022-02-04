package io.defitrack.staking.vo

import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import io.defitrack.protocol.staking.TokenType
import java.math.BigDecimal

data class StakingMarketElementVO(
    val id: String,
    val network: NetworkVO,
    val protocol: ProtocolVO,
    val name: String,
    val stakedToken: StakedTokenVO,
    val reward: List<RewardTokenVO>,
    val contractAddress: String,
    val vaultType: String,
    val marketSize: BigDecimal,
    val rate: BigDecimal = BigDecimal.ZERO
)

data class StakedTokenVO(
    val name: String,
    val symbol: String,
    val address: String,
    val network: NetworkVO,
    val decimals: Int,
    val type: TokenType
)

data class RewardTokenVO(
    var name: String = "",
    var symbol: String = "",
    var decimals: Int = 18
)