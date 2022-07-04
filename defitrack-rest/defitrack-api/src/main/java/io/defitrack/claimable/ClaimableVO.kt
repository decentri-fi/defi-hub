package io.defitrack.claimable

import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import io.defitrack.token.FungibleToken
import io.defitrack.transaction.PreparedTransaction

class ClaimableVO(
    val id: String,
    val name: String,
    val type: String,
    val protocol: ProtocolVO,
    val network: NetworkVO,
    val dollarValue: Double,
    val amount: Double,
    val token: FungibleToken,
    val claimTransaction: List<PreparedTransaction>
)