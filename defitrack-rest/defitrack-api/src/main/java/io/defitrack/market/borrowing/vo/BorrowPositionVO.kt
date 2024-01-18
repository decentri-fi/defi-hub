package io.defitrack.market.borrowing.vo

import io.defitrack.token.FungibleToken
import io.defitrack.network.NetworkInformation
import io.defitrack.protocol.ProtocolInformation
import java.math.BigDecimal

data class BorrowPositionVO(
    val id: String,
    val network: NetworkInformation,
    val protocol: ProtocolInformation,
    val dollarValue: Double,
    val name: String,
    val rate: BigDecimal?,
    val amount: Double,
    val underlyingAmount: Double,
    val token: FungibleToken
)