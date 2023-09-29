package io.defitrack.market.farming.domain

import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.exit.ExitPositionPreparer
import io.defitrack.market.DefiMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.token.FungibleToken
import java.math.BigDecimal

data class FarmingMarket(
    override val id: String,
    val network: Network,
    override val protocol: Protocol,
    val name: String,
    val stakedToken: FungibleToken,
    val rewardTokens: List<FungibleToken>,
    val contractType: String,
    val marketSize: Refreshable<BigDecimal>? = null,
    val apr: BigDecimal? = null,
    val farmType: ContractType,
    val balanceFetcher: PositionFetcher? = null,
    val investmentPreparer: InvestmentPreparer? = null,
    val exitPositionPreparer: ExitPositionPreparer? = null,
    val claimableRewardFetcher: ClaimableRewardFetcher? = null,
    val metadata: Map<String, Any> = emptyMap(),
    val internalMetadata: Map<String, Any> = emptyMap(),
    val expired: Boolean = false
) : DefiMarket(id, "farming", protocol) {

    init {
        addRefetchableValue(marketSize)
    }

}
