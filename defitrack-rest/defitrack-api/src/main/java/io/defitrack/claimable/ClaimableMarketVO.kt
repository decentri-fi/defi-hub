package io.defitrack.claimable

import io.defitrack.common.network.Network
import io.defitrack.network.NetworkVO
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.ProtocolVO

class ClaimableMarketVO(
    val id: String,
    val name: String,
    val network: NetworkVO,
    val protocol: ProtocolVO,
)