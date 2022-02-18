package io.defitrack.lending.domain

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.token.FungibleToken
import java.math.BigInteger

data class LendingElement(
    val id: String,
    val network: Network,
    val protocol: Protocol,
    val rate: Double? = null,
    val amount: BigInteger,
    val name: String,
    val token: FungibleToken
)