package io.defitrack.protocol.sushi.domain

import java.math.BigDecimal

class LiquidityPosition(
    val id: String,
    val pair: SushiswapPair,
    val liquidityTokenBalance: BigDecimal
)