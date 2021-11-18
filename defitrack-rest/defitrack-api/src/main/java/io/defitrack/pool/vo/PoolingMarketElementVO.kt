package io.defitrack.pool.vo

import io.codechef.defitrack.network.NetworkVO
import io.codechef.defitrack.protocol.ProtocolVO
import java.math.BigDecimal

data class PoolingMarketElementVO(
    val id: String,
    val address: String,
    val name: String,
    val protocol: ProtocolVO,
    val network: NetworkVO,
    val token: List<PoolingMarketElementToken>,
    val apr: BigDecimal = BigDecimal.ZERO,
    val marketSize: BigDecimal,
)

data class PoolingMarketElementToken(
    val name: String,
    val symbol: String,
    val address: String,
    val logo: String
)