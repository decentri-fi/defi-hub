package io.defitrack.protocol.quickswap.domain

import java.math.BigDecimal

class QuickswapPair(
    val id: String,
    val token0: QuickswapToken,
    val token1: QuickswapToken,
    val reserveUSD: BigDecimal
)

class QuickswapToken(
    val id: String,
    val decimals: Int,
    val symbol: String,
    val name: String
)