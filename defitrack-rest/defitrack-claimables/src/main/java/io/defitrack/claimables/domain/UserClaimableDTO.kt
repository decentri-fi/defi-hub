package io.defitrack.claimables.domain

import io.defitrack.erc20.FungibleTokenInformationVO
import io.defitrack.network.NetworkInformationVO
import io.defitrack.protocol.ProtocolVO
import io.defitrack.transaction.PreparedTransactionVO

class UserClaimableDTO(
    val id: String,
    val name: String,
    val protocol: ProtocolVO,
    val network: NetworkInformationVO,
    val dollarValue: Double,
    val amount: Double,
    val token: FungibleTokenInformationVO,
    val claimTransaction: PreparedTransactionVO?
)