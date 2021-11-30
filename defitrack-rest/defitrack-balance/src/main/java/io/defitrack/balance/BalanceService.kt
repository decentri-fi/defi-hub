package io.defitrack.balance

import io.defitrack.common.network.Network
import java.math.BigDecimal

interface BalanceService {
    fun getNetwork(): Network
    fun getNativeBalance(address: String): BigDecimal
    fun getTokenBalances(user: String): List<TokenBalance>
    fun nativeTokenName(): String
}