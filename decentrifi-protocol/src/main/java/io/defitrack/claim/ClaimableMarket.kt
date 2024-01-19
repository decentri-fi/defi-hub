package io.defitrack.claim

import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol

class ClaimableMarket(
    val id: String,
    val name: String,
    val network: Network,
    val protocol: Protocol,
    val claimableRewardFetchers: List<ClaimableRewardFetcher>,
)