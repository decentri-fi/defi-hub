package io.defitrack.protocol

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import java.math.BigInteger


interface ProtocolService {
    fun getProtocol(): Protocol
    fun getNetwork(): Network

    fun toDecimalValue(balance: BigInteger, decimals: Int): Double {
        return balance.asEth(decimals).toDouble()
    }
}