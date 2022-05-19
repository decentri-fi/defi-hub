package io.defitrack.lending.vo

import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import io.defitrack.token.FungibleToken
import java.math.BigDecimal

data class LendingMarketVO(
    val id: String,
    val name: String,
    val protocol: ProtocolVO,
    val network: NetworkVO,
    val token: FungibleToken,
    val rate: Double?,
    val poolType: String,
    val marketSize: BigDecimal?,
    val prepareInvestmentSupported: Boolean
)