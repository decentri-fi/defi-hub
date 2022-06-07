package io.defitrack.protocol.apeswap.domain

import java.math.BigDecimal

class LiquidityPool(
    val id: String,
    val name: String,
    val symbol: String,
    val totalValueLockedUSD: BigDecimal
)