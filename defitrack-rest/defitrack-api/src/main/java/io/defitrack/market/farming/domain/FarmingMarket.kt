package io.defitrack.market.farming.domain

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.token.FungibleToken
import java.math.BigDecimal


data class FarmingMarket(
    val id: String,
    val network: Network,
    val protocol: Protocol,
    val name: String,
    val stakedToken: FungibleToken,
    val rewardTokens: List<FungibleToken>,
    val contractAddress: String,
    val vaultType: String,
    val marketSize: BigDecimal? = null,
    val apr: BigDecimal? = null,
    val balanceFetcher: FarmingPositionFetcher? = null,
    val underlyingBalanceFetcher: FarmingPositionFetcher? = null,
    val investmentPreparer: InvestmentPreparer? = null
)
