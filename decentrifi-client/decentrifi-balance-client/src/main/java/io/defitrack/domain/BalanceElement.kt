package io.defitrack.domain

import io.defitrack.adapter.output.domain.erc20.FungibleTokenInformation
import io.defitrack.adapter.output.domain.meta.NetworkInformationDTO
import java.math.BigInteger


data class BalanceElement(
    val amount: BigInteger,
    val network: NetworkInformationDTO,
    val token: FungibleTokenInformation,
    val price: Double,
    val decimalAmount: Double,
    val dollarValue: Double
)