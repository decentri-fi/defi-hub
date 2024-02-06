package io.defitrack.market.pooling.vo

import io.defitrack.erc20.domain.FungibleTokenInformation
import java.math.BigDecimal
import java.math.BigInteger

class PoolingMarketTokenShareVO(
    val token: FungibleTokenInformation,
    val reserve: BigInteger,
    val reserveDecimal: BigDecimal
)