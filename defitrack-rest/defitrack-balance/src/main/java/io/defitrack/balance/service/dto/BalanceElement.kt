package io.defitrack.balance.service.dto

import io.defitrack.erc20.FungibleToken
import io.defitrack.network.NetworkVO

data class BalanceElement(
    val amount: Double,
    val network: NetworkVO,
    val token: FungibleToken,
    val dollarValue: Double,
    val price: Double
)