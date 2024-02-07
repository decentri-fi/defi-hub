package io.defitrack.protocol.uniswap.v2.pooling.prefetch

import java.math.BigInteger

class PoolingMarketTokenShareInformation(
    val token: FungibleTokenInformation,
    val reserve: BigInteger
)