package io.defitrack.claimable

import io.codechef.defitrack.network.NetworkVO
import io.codechef.defitrack.protocol.ProtocolVO

class ClaimableElementVO(
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