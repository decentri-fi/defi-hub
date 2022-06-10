package io.defitrack.protocol.kyberswap.domain

import io.defitrack.protocol.kyberswap.domain.KyberSwapToken
import io.defitrack.protocol.kyberswap.domain.Pair
import java.math.BigDecimal

class Pool(
    val id: String,
    val pair: Pair,
    val token0: KyberSwapToken,
    val token1: KyberSwapToken,
    val reserveUSD: BigDecimal
)