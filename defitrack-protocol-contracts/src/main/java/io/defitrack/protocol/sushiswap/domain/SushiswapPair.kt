package io.defitrack.protocol.sushiswap.domain

import java.math.BigDecimal

class SushiswapPair(
    val id: String,
    val token0: SushiswapToken,
    val token1: SushiswapToken,
    val reserveUSD: BigDecimal,
)

class SushiswapToken(
    val id: String,
    val decimals: Int,
    val symbol: String,
    val name: String
)