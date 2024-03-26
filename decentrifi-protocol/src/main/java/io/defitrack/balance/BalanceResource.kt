package io.defitrack.balance

import io.defitrack.balance.domain.BalanceElement
import io.defitrack.common.network.Network
import java.math.BigInteger

interface BalanceResource {
    suspend fun getNativeBalance(network: Network, user: String): BalanceElement
    suspend fun getNativeBalances(user: String): List<BalanceElement>
    suspend fun getTokenBalance(
        networkName: Network,
        user: String,
        token: String
    ): BalanceElement
}