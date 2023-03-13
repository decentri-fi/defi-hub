package io.defitrack.market.farming.domain

import java.math.BigInteger

data class FarmingPosition(
    val market: FarmingMarket,
    val underlyingAmount: BigInteger,
    val tokenAmount: BigInteger,
)
