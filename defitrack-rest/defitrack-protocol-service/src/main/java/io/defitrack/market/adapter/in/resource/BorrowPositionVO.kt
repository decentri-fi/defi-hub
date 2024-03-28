package io.defitrack.market.adapter.`in`.resource

import io.defitrack.erc20.FungibleTokenInformationVO
import io.defitrack.network.NetworkInformationVO
import io.defitrack.protocol.ProtocolVO
import java.math.BigDecimal

data class BorrowPositionVO(
    val id: String,
    val network: NetworkInformationVO,
    val protocol: ProtocolVO,
    val dollarValue: Double,
    val name: String,
    val rate: BigDecimal?,
    val amount: Double,
    val underlyingAmount: Double,
    val token: FungibleTokenInformationVO
)