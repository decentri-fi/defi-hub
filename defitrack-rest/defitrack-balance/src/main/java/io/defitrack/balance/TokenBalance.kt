package io.defitrack.balance

import io.defitrack.common.network.Network
import java.math.BigInteger

class TokenBalance(
    val address: String,
    val amount: BigInteger,
    val decimals: Int,
    val symbol: String,
    val name: String,
    val network: Network
)