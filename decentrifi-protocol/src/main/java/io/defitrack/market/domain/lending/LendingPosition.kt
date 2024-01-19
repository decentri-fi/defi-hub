package io.defitrack.market.domain.lending

import io.defitrack.market.domain.lending.LendingMarket
import java.math.BigInteger

data class LendingPosition(
    val underlyingAmount: BigInteger,
    val tokenAmount: BigInteger,
    val market: LendingMarket
)