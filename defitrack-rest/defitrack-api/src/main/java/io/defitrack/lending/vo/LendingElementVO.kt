package io.defitrack.lending.vo

import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO

data class LendingElementVO(
    val id: String,
    val user: String,
    val network: NetworkVO,
    val protocol: ProtocolVO,
    val dollarValue: Double,
    val rate: Double?,
    val name: String,
    val amount: String,
    val symbol: String
)