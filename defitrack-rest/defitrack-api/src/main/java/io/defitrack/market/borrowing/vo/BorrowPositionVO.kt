package io.defitrack.market.borrowing.vo

import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import io.defitrack.token.FungibleToken

data class BorrowPositionVO(
    val id: String,
    val network: NetworkVO,
    val protocol: ProtocolVO,
    val dollarValue: Double,
    val name: String,
    val rate: Double?,
    val amount: Double,
    val token: FungibleToken
)