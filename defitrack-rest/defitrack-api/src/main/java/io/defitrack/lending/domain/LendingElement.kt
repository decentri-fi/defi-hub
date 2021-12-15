package io.defitrack.lending.domain

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol

data class LendingElement constructor(
    var id: String,
    var user: String = "",
    var network: Network,
    var platform: String = "",
    var protocol: Protocol,
    var rate: Double? = null,
    var name: String = "unknown",
    var amount: String = "",
    var symbol: String = "?"
)