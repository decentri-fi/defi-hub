package io.defitrack.protocol.dmm

import java.math.BigDecimal

class LiquidityPosition(
    val id: String,
    val pool: DMMPool,
    val liquidityTokenBalance: BigDecimal
)