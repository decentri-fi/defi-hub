package io.defitrack.market.lending.vo

import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import io.defitrack.token.FungibleToken
import java.math.BigInteger

data class LendingElementVO(
    val id: String,
    val network: NetworkVO,
    val protocol: ProtocolVO,
    val dollarValue: Double,
    val rate: Double?,
    val name: String,
    val amount: Double,
    val token: FungibleToken
)