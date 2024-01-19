package io.defitrack.market.adapter.`in`.resource

import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.networkinfo.NetworkInformation
import io.defitrack.protocol.ProtocolVO
import java.math.BigDecimal

data class BorrowPositionVO(
    val id: String,
    val network: NetworkInformation,
    val protocol: ProtocolVO,
    val dollarValue: Double,
    val name: String,
    val rate: BigDecimal?,
    val amount: Double,
    val underlyingAmount: Double,
    val token: FungibleTokenInformation
)