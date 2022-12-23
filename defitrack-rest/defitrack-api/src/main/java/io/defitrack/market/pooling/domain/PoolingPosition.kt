package io.defitrack.market.pooling.domain

import java.math.BigInteger

class PoolingPosition(
    val amount: BigInteger,
    val market: PoolingMarket
)