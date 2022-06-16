package io.defitrack.market.borrowing.domain

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.token.FungibleToken
import java.math.BigInteger

data class BorrowPosition(
    var id: String,
    var network: Network,
    var protocol: Protocol,
    var name: String,
    var rate: Double? = null,
    var amount: BigInteger,
    val token: FungibleToken
)

