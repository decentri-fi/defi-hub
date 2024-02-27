package io.defitrack.protocol.uniswap.v3.prefetch

import java.math.BigInteger

class PoolingMarketTokenShareInformation(
    val token: FungibleTokenInformation,
    val reserve: BigInteger
)