package io.defitrack.protocol.application.pancakeswap.prefetch

import java.math.BigInteger

class PoolingMarketTokenShareInformation(
    val token: FungibleTokenInformation,
    val reserve: BigInteger
)