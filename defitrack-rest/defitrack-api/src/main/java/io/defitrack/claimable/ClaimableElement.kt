package io.defitrack.claimable

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol


class ClaimableElement(
    val name: String,
    val address: String,
    val type: String,
    val protocol: Protocol,
    val network: Network,
    val claimableToken: ClaimableToken
)

class ClaimableToken(
    val name: String,
    val symbol: String,
    val amount: Double?,
)