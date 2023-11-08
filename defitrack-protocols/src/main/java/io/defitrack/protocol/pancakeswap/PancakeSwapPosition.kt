package io.defitrack.pancakeswap

import java.math.BigInteger

data class PancakeSwapPosition(
    val token0: String,
    val token1: String,
    val fee: BigInteger,
    val liquidity: BigInteger,
    val tickLower: BigInteger,
    val tickUpper: BigInteger,
    val feeGrowthInside0LastX128: BigInteger,
    val feeGrowthInside1LastX128: BigInteger,
)