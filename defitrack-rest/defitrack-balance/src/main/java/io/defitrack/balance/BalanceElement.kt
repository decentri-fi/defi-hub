package io.defitrack.balance

import io.defitrack.network.NetworkVO

data class BalanceElement(
    val amount: Double,
    val network: NetworkVO,
    val address: String,
    val symbol: String,
    val name: String,
    val dollarValue: Double,
    val logo: String
)