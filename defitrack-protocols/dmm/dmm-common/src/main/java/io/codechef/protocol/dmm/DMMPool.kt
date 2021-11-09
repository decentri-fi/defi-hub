package io.defitrack.protocol.dmm

import java.math.BigDecimal

class DMMPool(
    val id: String,
    val pair: DMMPair,
    val token0: DMMToken,
    val token1: DMMToken,
    val reserveUSD: BigDecimal
)