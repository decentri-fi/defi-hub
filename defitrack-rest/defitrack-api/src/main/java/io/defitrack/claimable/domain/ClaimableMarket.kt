package io.defitrack.claimable.domain

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol

class ClaimableMarket(
    val id: String,
    val name: String,
    val network: Network,
    val protocol: Protocol,
    val claimableRewardFetcher: ClaimableRewardFetcher,
)