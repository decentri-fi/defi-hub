package io.defitrack.market.borrowing.vo

import io.defitrack.erc20.FungibleToken
import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import java.math.BigDecimal

data class BorrowPositionVO(
    val id: String,
    val network: NetworkVO,
    val protocol: ProtocolVO,
    val dollarValue: Double,
    val name: String,
    val rate: BigDecimal?,
    val amount: Double,
    val underlyingAmount: Double,
    val token: FungibleToken
)