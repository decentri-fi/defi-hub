package io.defitrack.market.pooling.vo

import io.defitrack.token.FungibleToken
import java.math.BigDecimal
import java.math.BigInteger

class PoolingPositionTokenshareVO(
    val token: FungibleToken,
    val reserve: BigInteger,
    val reserveUSD: BigDecimal
)