package io.defitrack.market.borrowing.vo

import io.defitrack.domain.FungibleToken
import io.defitrack.domain.NetworkInformation
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
    val token: FungibleToken
)