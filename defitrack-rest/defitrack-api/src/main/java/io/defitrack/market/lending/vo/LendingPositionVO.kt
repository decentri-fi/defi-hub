package io.defitrack.market.lending.vo

import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import io.defitrack.token.FungibleToken
import java.math.BigInteger

data class LendingPositionVO(
    val id: String,
    val network: NetworkVO,
    val protocol: ProtocolVO,
    val dollarValue: Double,
    val rate: Double?,
    val name: String,
    val amount: Double,
    val nativeAmount: BigInteger,
    val token: FungibleToken,
    val exitPositionSupported: Boolean,
    val marketType: String = "lending"
)