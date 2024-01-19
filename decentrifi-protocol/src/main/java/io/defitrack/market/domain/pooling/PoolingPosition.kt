package io.defitrack.market.domain.pooling

import io.defitrack.market.domain.PoolingMarket
import java.math.BigInteger

class PoolingPosition(
    val tokenAmount: BigInteger,
    val market: PoolingMarket,
    val customPriceCalculator: PriceCalculator? = null
)

interface PriceCalculator{
    fun calculate(): Double
}