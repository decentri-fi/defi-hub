package io.defitrack.claimables.domain

import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.networkinfo.NetworkInformation
import io.defitrack.protocol.ProtocolVO
import io.defitrack.transaction.PreparedTransactionVO

class UserClaimableDTO(
    val id: String,
    val name: String,
    val protocol: ProtocolVO,
    val network: NetworkInformation,
    val dollarValue: Double,
    val amount: Double,
    val token: FungibleTokenInformation,
    val claimTransaction: PreparedTransactionVO?
)