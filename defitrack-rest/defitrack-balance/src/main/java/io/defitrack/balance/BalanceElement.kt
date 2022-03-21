package io.defitrack.balance

import io.defitrack.network.NetworkVO
import io.defitrack.token.FungibleToken
import io.defitrack.token.TokenInformation

data class BalanceElement(
    val amount: Double,
    val network: NetworkVO,
    val token: TokenInformation,
    val dollarValue: Double,
)