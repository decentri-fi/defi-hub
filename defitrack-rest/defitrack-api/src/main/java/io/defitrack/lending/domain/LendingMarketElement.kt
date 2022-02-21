package io.defitrack.lending.domain

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.token.FungibleToken


data class LendingMarketElement(
    val id: String,
    val network: Network,
    val protocol: Protocol,
    val address: String,
    val name: String,
    val token: FungibleToken,
    val marketSize: Double,
    val rate: Double,
    val poolType: String
)