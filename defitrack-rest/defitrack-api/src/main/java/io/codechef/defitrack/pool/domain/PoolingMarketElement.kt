package io.codechef.defitrack.pool.domain

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import java.math.BigDecimal

data class PoolingMarketElement(
    val id: String,
    val network: Network,
    val protocol: Protocol,
    val address: String,
    val name: String,
    val token: List<PoolingToken>,
    val apr: BigDecimal = BigDecimal.ZERO,
    val marketSize: BigDecimal
)

data class PoolingToken(
    val name: String,
    val symbol: String,
    val address: String
)