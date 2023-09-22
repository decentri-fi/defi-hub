package io.defitrack.market.pooling.domain

import io.defitrack.token.FungibleToken
import java.math.BigDecimal
import java.math.BigInteger

data class PoolingMarketTokenShare(
    val token: FungibleToken,
    val reserveUSD: BigDecimal
)