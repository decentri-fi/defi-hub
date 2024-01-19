package io.defitrack.claimable.vo

import io.defitrack.domain.FungibleToken
import io.defitrack.domain.NetworkInformation
import io.defitrack.protocol.ProtocolVO
import io.defitrack.transaction.PreparedTransactionVO

class UserClaimableVO(
    val id: String,
    val name: String,
    val protocol: ProtocolVO,
    val network: NetworkInformation,
    val dollarValue: Double,
    val amount: Double,
    val token: FungibleToken,
    val claimTransaction: PreparedTransactionVO?
)