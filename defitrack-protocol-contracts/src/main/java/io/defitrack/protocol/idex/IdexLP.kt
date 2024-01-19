package io.defitrack.protocol.idex

import java.math.BigDecimal

data class IdexLP(
    val tokenA: String,
    val tokenB: String,
    val liquidityToken: String,
    val reserveUsd: BigDecimal
)