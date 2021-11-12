package io.defitrack.protocol.dfyn.domain

import java.math.BigDecimal

class Pair(
    val id: String,
    val token0: Token,
    val token1: Token,
    val reserveUSD: BigDecimal,
)

class Token(
    val id: String,
    val decimals: Int,
    val symbol: String,
    val name: String
)