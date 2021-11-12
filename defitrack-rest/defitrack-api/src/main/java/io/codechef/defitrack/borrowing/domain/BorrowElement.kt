package io.codechef.defitrack.borrowing.domain

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol

data class BorrowElement(
    var id: String = "",
    var user: String = "",
    var network: Network = Network.ETHEREUM,
    var platform: String = "",
    var protocol: Protocol = Protocol.YEARN,
    var name: String = "unknown",
    var rate: Double? = null,
    var amount: String = "?",
    var symbol: String = "?",
    var tokenUrl: String = "?"
)