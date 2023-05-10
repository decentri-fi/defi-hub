package io.defitrack.uniswap.v3

import java.math.BigInteger

data class UniswapPosition(
    val token0: String,
    val token1: String,
    val liquidity: BigInteger,
)