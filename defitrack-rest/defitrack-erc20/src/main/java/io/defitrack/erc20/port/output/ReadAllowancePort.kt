package io.defitrack.erc20.port.output

import io.defitrack.common.network.Network
import java.math.BigInteger

interface ReadAllowancePort {
    suspend fun getAllowance(network: Network, address: String, userAddress: String, spenderAddress: String): BigInteger
}