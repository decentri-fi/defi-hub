package io.defitrack.balance.service.dto

import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.networkinfo.NetworkInformation

data class BalanceElement(
    val amount: Double,
    val network: NetworkInformation,
    val token: FungibleTokenInformation,
    val dollarValue: Double,
    val price: Double
)