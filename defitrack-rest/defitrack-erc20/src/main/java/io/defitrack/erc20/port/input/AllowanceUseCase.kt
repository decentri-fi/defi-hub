package io.defitrack.erc20.port.input

import io.defitrack.common.network.Network
import java.math.BigInteger

interface AllowanceUseCase {

    suspend fun getAllowance(
        network: Network,
        owner: String,
        spender: String,
        tokenAddress: String
    ): BigInteger
}