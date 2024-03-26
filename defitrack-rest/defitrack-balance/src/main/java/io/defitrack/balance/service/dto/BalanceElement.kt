package io.defitrack.balance.service.dto

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.networkinfo.NetworkInformation
import java.math.BigInteger

data class BalanceElement(
    val amount: BigInteger,
    val network: NetworkInformation,
    val token: FungibleTokenInformation,
    val price: Double
) {
    val decimalAmount = amount.asEth(token.decimals).toDouble()
    val dollarValue = price.times(decimalAmount)

}