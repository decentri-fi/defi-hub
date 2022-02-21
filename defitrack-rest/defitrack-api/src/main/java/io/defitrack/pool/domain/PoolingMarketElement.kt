package io.defitrack.pool.domain

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.token.FungibleToken
import java.math.BigDecimal

data class PoolingMarketElement(
    val id: String,
    val network: Network,
    val protocol: Protocol,
    val address: String,
    val name: String,
    val token: List<FungibleToken>,
    val apr: BigDecimal = BigDecimal.ZERO,
    val marketSize: BigDecimal
)