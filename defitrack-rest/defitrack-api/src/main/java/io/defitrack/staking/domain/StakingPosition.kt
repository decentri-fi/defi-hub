package io.defitrack.staking.domain

import java.math.BigInteger

data class StakingPosition(
    val market: StakingMarket,
    val amount: BigInteger
)
