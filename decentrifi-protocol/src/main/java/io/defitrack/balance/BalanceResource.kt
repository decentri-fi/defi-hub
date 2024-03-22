package io.defitrack.balance

import io.defitrack.common.network.Network
import java.math.BigInteger

interface BalanceResource {
    suspend fun getNativeBalance(network: Network, user: String): DecentrifiBalanceResource.BalanceElement
}