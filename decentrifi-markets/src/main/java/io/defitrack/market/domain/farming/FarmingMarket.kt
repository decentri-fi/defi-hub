package io.defitrack.market.domain.farming

import io.defitrack.adapter.output.domain.erc20.FungibleTokenInformation
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.invest.InvestmentPreparer
import io.defitrack.market.domain.DefiMarket
import io.defitrack.market.domain.position.ExitPositionPreparer
import io.defitrack.protocol.Protocol
import java.math.BigDecimal

data class FarmingMarket(
    override val id: String,
    override val network: Network,
    override val protocol: Protocol,
    override val type: String,
    val name: String,
    val stakedToken: FungibleTokenInformation,
    val rewardTokens: List<FungibleTokenInformation>,
    val marketSize: Refreshable<BigDecimal>? = null,
    val apr: BigDecimal? = null,
    val balanceFetcher: PositionFetcher? = null,
    val investmentPreparer: InvestmentPreparer? = null,
    val exitPositionPreparer: ExitPositionPreparer? = null,
    val claimableRewardFetchers: List<ClaimableRewardFetcher> = emptyList(),
    val metadata: Map<String, Any> = emptyMap(),
    val internalMetadata: Map<String, Any> = emptyMap(),
    override val deprecated: Boolean = false,
    val token: FungibleTokenInformation? = null
) : DefiMarket(id, "farming", type, protocol, network, deprecated)
