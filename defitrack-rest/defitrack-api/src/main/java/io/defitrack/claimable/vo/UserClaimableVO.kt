package io.defitrack.claimable.vo

import io.defitrack.token.FungibleToken
import io.defitrack.network.NetworkInformation
import io.defitrack.protocol.ProtocolInformation
import io.defitrack.transaction.PreparedTransactionVO

class UserClaimableVO(
    val id: String,
    val name: String,
    val protocol: ProtocolInformation,
    val network: NetworkInformation,
    val dollarValue: Double,
    val amount: Double,
    val token: FungibleToken,
    val claimTransaction: PreparedTransactionVO?
)