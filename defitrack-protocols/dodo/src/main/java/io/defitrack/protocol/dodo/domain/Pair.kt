package io.defitrack.protocol.dodo.domain

import java.math.BigDecimal

class Pair(
    val id: String,
    val baseToken: Token,
    val quoteToken: Token,
    val volumeUSD: BigDecimal
)

class Token(
    val id: String
)