package io.defitrack.staking.domain

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.token.FungibleToken
import io.defitrack.token.TokenType
import java.math.BigDecimal


data class StakingMarketElement(
    val id: String,
    val network: Network,
    val protocol: Protocol,
    val name: String,
    val token: FungibleToken,
    val reward: List<FungibleToken>,
    val contractAddress: String,
    val vaultType: String,
    val marketSize: BigDecimal = BigDecimal.ZERO,
    val rate: BigDecimal = BigDecimal.ZERO
)
