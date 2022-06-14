package io.defitrack.messari

import java.math.BigDecimal

class LiquidityPool(
    val id: String,
    val name: String,
    val rewardTokens: List<RewardToken>,
    val totalValueLockedUSD: BigDecimal
)

class RewardToken(
    val id: String
)