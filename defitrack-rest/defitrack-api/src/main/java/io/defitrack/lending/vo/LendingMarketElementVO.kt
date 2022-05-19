package io.defitrack.lending.vo

import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import java.math.BigDecimal

data class LendingMarketElementVO(
    val name: String,
    val protocol: ProtocolVO,
    val network: NetworkVO,
    val token: LendingMarketElementToken,
    val rate: Double?,
    val poolType: String,
    val marketSize: BigDecimal?
)

data class LendingMarketElementToken(
    val name: String,
    val symbol: String,
    val address: String,
    val logo: String?
)