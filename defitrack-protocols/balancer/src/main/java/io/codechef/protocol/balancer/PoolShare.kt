package io.defitrack.protocol.balancer

import java.math.BigDecimal

class PoolShare(
    val poolId: Pool,
    val balance: BigDecimal
)

class Pool(
    val id: String,
    val tokens: List<PoolToken>,
    val symbol: String,
    val name: String,
    val active: Boolean,
    val address: String,
    val totalShares: BigDecimal,
    val totalLiquidity: BigDecimal
)

class PoolToken(
    val id: String,
    val symbol: String,
    val name: String,
    val decimals: Int,
    val address: String,
    val balance: BigDecimal,
)