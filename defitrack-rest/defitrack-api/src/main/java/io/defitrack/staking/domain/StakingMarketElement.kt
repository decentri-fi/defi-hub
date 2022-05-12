package io.defitrack.staking.domain

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.multicall.MultiCallElement
import io.defitrack.protocol.Protocol
import io.defitrack.token.FungibleToken
import java.math.BigDecimal


data class StakingMarketElement(
    val id: String,
    val network: Network,
    val protocol: Protocol,
    val name: String,
    val stakedToken: FungibleToken,
    val rewardTokens: List<FungibleToken>,
    val contractAddress: String,
    val vaultType: String,
    val marketSize: BigDecimal = BigDecimal.ZERO,
    val rate: BigDecimal = BigDecimal.ZERO,
    val balanceFetcher: StakingMarketBalanceFetcher? = null
)
