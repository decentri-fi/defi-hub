package io.defitrack.claimable

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol

class Claimable(
    val id: String,
    val name: String,
    val network: Network,
    val protocol: Protocol,
    val claimableRewardFetcher: ClaimableRewardFetcher,
)