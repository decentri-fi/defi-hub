package io.defitrack.market.domain.pooling

import io.defitrack.adapter.output.domain.erc20.FungibleTokenInformation
import java.math.BigDecimal
import java.math.BigInteger

class PoolingMarketTokenShareInformation(
    val token: FungibleTokenInformation,
    val reserve: BigInteger,
    val reserveDecimal: BigDecimal
)