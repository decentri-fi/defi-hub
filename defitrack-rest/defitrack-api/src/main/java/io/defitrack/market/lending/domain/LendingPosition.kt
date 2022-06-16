package io.defitrack.market.lending.domain

import java.math.BigInteger

data class LendingPosition(
    val amount: BigInteger,
    val market: LendingMarket
)