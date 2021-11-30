package io.defitrack.balance

import io.defitrack.common.network.Network
import java.math.BigDecimal

class TokenBalance(
    val address: String,
    val amount: BigDecimal,
    val decimals: Int,
    val symbol: String,
    val name: String,
    val network: Network
)