package io.defitrack.lending.domain

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol


data class LendingMarketElement(
    val id: String,
    val network: Network,
    val protocol: Protocol,
    val address: String,
    val name: String,
    val token: LendingToken,
    val marketSize: Double,
    val rate: Double,
    val poolType: String
)

data class LendingToken(
    val name: String,
    val symbol: String,
    val address: String
)