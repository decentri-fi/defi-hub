package io.codechef.defitrack.protocol

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode


interface ProtocolService {
    fun getProtocol(): Protocol
    fun getNetwork(): Network

    fun toDecimalValue(balance: BigInteger, decimals: Int): Double {
        return balance.toBigDecimal()
            .divide(BigDecimal.TEN.pow(decimals), 18, RoundingMode.HALF_UP).toDouble()
    }
}