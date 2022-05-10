package io.defitrack.staking.domain

import java.math.BigInteger

data class StakingElement(
    val market: StakingMarketElement,
    val amount: BigInteger
)
