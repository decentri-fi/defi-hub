package io.defitrack.market.lending.domain

import java.math.BigInteger

data class LendingPosition(
    val underlyingAmount: BigInteger,
    val tokenAmount: BigInteger,
    val market: LendingMarket
)