package io.defitrack.market.domain

import io.defitrack.adapter.output.domain.erc20.FungibleTokenInformation
import java.math.BigInteger

data class PoolingMarketTokenShare(
    val token: FungibleTokenInformation,
    val reserve: BigInteger,
)

fun FungibleTokenInformation.asShare(reserve: BigInteger): PoolingMarketTokenShare =
    PoolingMarketTokenShare(
        token = this,
        reserve = reserve
    )