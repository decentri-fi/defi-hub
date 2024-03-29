package io.defitrack.market.pooling.vo

import io.defitrack.erc20.FungibleTokenInformationVO
import java.math.BigDecimal
import java.math.BigInteger

class PoolingPositionTokenshareVO(
    val token: FungibleTokenInformationVO,
    val reserve: BigInteger,
    val reserveDecimal: BigDecimal,
    val reserveUSD: BigDecimal
)