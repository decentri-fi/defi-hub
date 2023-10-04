package io.defitrack.protocol.dfyn.domain

import java.math.BigDecimal

class LiquidityPosition(
    val id: String,
    val pair: Pair,
    val liquidityTokenBalance: BigDecimal
)