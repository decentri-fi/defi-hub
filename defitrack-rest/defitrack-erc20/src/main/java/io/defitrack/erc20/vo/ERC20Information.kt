package io.defitrack.erc20.vo

import io.defitrack.network.NetworkVO

data class ERC20Information(
    val logo: String?,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val network: NetworkVO,
    val address: String,
    val dollarValue: Double,
)