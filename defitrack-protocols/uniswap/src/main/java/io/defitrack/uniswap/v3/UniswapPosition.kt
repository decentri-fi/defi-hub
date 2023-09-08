package io.defitrack.uniswap.v3

import java.math.BigInteger

data class UniswapPosition(
    val token0: String,
    val token1: String,
    val fee: BigInteger,
    val liquidity: BigInteger,
    val tickLower: BigInteger,
    val tickUpper: BigInteger,
    val feeGrowthInside0LastX128: BigInteger,
    val feeGrowthInside1LastX128: BigInteger,
)