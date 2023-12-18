package io.defitrack.market.pooling.domain

import io.defitrack.erc20.FungibleToken
import java.math.BigDecimal
import java.math.BigInteger

data class PoolingMarketTokenShare(
    val token: FungibleToken,
    val reserve: BigInteger,
    val reserveUSD: BigDecimal
)

fun List<PoolingMarketTokenShare>.marketSize(): BigDecimal {
    return this.sumOf { it.reserveUSD }
}