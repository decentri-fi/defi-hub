package io.defitrack.protocol.kyberswap.graph.domain

import java.math.BigDecimal

class Pool(
    val id: String,
    val pair: Pair,
    val token0: KyberSwapToken,
    val token1: KyberSwapToken,
    val reserveUSD: BigDecimal
)