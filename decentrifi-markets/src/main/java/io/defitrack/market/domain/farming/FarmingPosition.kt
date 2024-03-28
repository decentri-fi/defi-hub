package io.defitrack.market.domain.farming

import io.defitrack.market.domain.farming.FarmingMarket
import java.math.BigInteger

data class FarmingPosition(
    val market: FarmingMarket,
    val underlyingAmount: BigInteger,
    val tokenAmount: BigInteger,
)
