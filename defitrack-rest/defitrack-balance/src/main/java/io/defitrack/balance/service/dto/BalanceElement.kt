package io.defitrack.balance.service.dto

import io.defitrack.domain.FungibleToken
import io.defitrack.domain.NetworkInformation

data class BalanceElement(
    val amount: Double,
    val network: NetworkInformation,
    val token: FungibleToken,
    val dollarValue: Double,
    val price: Double
)