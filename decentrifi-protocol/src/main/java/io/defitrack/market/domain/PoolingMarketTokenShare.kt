package io.defitrack.market.domain

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.erc20.domain.FungibleTokenInformation
import java.math.BigDecimal
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