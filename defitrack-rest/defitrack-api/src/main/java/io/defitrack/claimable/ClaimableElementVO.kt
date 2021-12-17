package io.defitrack.claimable

import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO

class ClaimableElementVO(
    val id: String,
    val address: String,
    val name: String,
    val type: String,
    val protocol: ProtocolVO,
    val network: NetworkVO,
    val claimableToken: ClaimableTokenVO
)

class ClaimableTokenVO(
    val name: String,
    val symbol: String,
    val amount: Double?,
    val dollarValue: Double
)