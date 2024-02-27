package io.defitrack.protocol.application.uniswap.v2.prefetch

import java.math.BigInteger

class PoolingMarketTokenShareInformation(
    val token: FungibleTokenInformation,
    val reserve: BigInteger
)