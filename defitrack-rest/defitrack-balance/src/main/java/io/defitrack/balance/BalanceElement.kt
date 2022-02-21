package io.defitrack.balance

import io.defitrack.network.NetworkVO
import io.defitrack.token.FungibleToken

data class BalanceElement(
    val amount: Double,
    val network: NetworkVO,
    val token: FungibleToken,
    val dollarValue: Double,
    val logo: String
)