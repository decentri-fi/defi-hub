package io.defitrack.market.farming.domain

import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.common.network.Network
import io.defitrack.market.DefiMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.FarmType
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
    val contractType: String,
    val marketSize: BigDecimal? = null,
    val apr: BigDecimal? = null,
    val farmType: FarmType,
    val balanceFetcher: PositionFetcher? = null,
    val investmentPreparer: InvestmentPreparer? = null,
    val claimableRewardFetcher: ClaimableRewardFetcher? = null,
    val metadata: Map<String, Any> = emptyMap(),
    val rewardsFinished: Boolean = false
) : DefiMarket
