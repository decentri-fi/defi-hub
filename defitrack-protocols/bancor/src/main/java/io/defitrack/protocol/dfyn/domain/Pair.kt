package io.defitrack.protocol.dfyn.domain

import java.math.BigDecimal

class Pair(
    val id: String,
    val token0: DfynToken,
    val token1: DfynToken,
    val reserveUSD: BigDecimal,
)

class DfynToken(
    val id: String,
    val decimals: Int,
    val symbol: String,
    val name: String
)