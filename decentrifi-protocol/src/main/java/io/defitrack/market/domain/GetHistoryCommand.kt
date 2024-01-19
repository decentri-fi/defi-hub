package io.defitrack.market.domain

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import java.math.BigInteger

data class GetHistoryCommand(
    val protocol: Protocol,
    val network: Network,
    val user: String,
    val fromBlock: BigInteger?,
    val toBlock: BigInteger?,
    val markets: List<String>?
)