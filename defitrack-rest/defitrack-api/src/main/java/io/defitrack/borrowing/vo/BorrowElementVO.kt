package io.defitrack.borrowing.vo

import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import io.defitrack.token.FungibleToken
import java.math.BigInteger

data class BorrowElementVO(
    val id: String,
    val network: NetworkVO,
    val protocol: ProtocolVO,
    val dollarValue: Double,
    val name: String,
    val rate: Double?,
    val amount: BigInteger,
    val token: FungibleToken
)