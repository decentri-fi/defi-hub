package io.defitrack.uniswap.v3

import io.defitrack.uniswap.v2.domain.UniswapToken

data class UniswapV3Pool(
    val id: String,
    val token0: UniswapToken,
    val token1: UniswapToken,
)