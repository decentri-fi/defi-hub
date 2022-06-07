package io.defitrack.protocol.domain

import java.math.BigDecimal

class LiquidityPool(
    val id: String,
    val totalValueLockedUSD: BigDecimal,
    val inputTokenWeights: List<BigDecimal>,
    val inputTokens: List<Token>
)

class Token(
    val id: String
)