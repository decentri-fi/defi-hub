package io.defitrack.uniswap.domain

import java.math.BigDecimal

class UniswapPair(
    val id: String,
    val token0: UniswapToken,
    val token1: UniswapToken,
    val reserveUSD: BigDecimal
) {
}

class UniswapToken(
    val id: String,
    val decimals: Int,
    val symbol: String,
    val name: String
)