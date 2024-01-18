package io.defitrack.balance.service.dto

import io.defitrack.token.FungibleToken
import io.defitrack.network.NetworkInformation

data class BalanceElement(
    val amount: Double,
    val network: NetworkInformation,
    val token: FungibleToken,
    val dollarValue: Double,
    val price: Double
)