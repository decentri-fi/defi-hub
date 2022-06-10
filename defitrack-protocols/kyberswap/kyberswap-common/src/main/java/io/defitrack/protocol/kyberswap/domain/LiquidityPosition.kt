package io.defitrack.protocol.kyberswap.domain

import java.math.BigDecimal

class LiquidityPosition(
    val id: String,
    val pool: Pool,
    val liquidityTokenBalance: BigDecimal
)