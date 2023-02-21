package io.defitrack.protocol.kyberswap.graph.domain

import io.defitrack.protocol.kyberswap.graph.domain.Pool
import java.math.BigDecimal

class LiquidityPosition(
    val id: String,
    val pool: Pool,
    val liquidityTokenBalance: BigDecimal
)