package io.defitrack.balance.domain

import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.networkinfo.NetworkInformation
import java.math.BigInteger


data class BalanceElement(
    val amount: BigInteger,
    val network: NetworkInformation,
    val token: FungibleTokenInformation,
    val price: Double,
    val decimalAmount: Double,
    val dollarValue: Double
)