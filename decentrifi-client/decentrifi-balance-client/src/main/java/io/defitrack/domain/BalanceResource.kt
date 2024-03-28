package io.defitrack.domain

import io.defitrack.common.network.Network

interface BalanceResource {
    suspend fun getNativeBalance(network: Network, user: String): BalanceElement
    suspend fun getNativeBalances(user: String): List<BalanceElement>
    suspend fun getTokenBalance(
        networkName: Network,
        user: String,
        token: String
    ): BalanceElement
}