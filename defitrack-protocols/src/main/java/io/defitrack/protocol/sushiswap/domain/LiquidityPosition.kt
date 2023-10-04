package io.defitrack.protocol.sushiswap.domain

import java.math.BigDecimal

class LiquidityPosition(
    val id: String,
    val pair: SushiswapPair,
    val liquidityTokenBalance: BigDecimal
)