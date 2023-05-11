package io.defitrack.market.pooling.domain

import java.math.BigInteger

class PoolingPosition(
    val tokenAmount: BigInteger,
    val market: PoolingMarket,
    val customPriceCalculator: PriceCalculator? = null
)

interface PriceCalculator{
    fun calculate(): Double
}