package io.defitrack.erc20.port.output

import io.defitrack.common.network.Network
import java.math.BigDecimal
import java.math.BigInteger

interface ReadBalancePort {
    suspend fun getBalance(network: Network, address: String, userAddress: String): BigInteger
    suspend fun getNativeBalance(network: Network, userAddress: String): BigDecimal

}