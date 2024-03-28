package io.defitrack.balance.service.dto

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.erc20.FungibleTokenInformationVO
import io.defitrack.network.NetworkInformationVO
import java.math.BigInteger

data class BalanceElementVO(
    val amount: BigInteger,
    val network: NetworkInformationVO,
    val token: FungibleTokenInformationVO,
    val price: Double
) {
    val decimalAmount = amount.asEth(token.decimals).toDouble()
    val dollarValue = price.times(decimalAmount)

}