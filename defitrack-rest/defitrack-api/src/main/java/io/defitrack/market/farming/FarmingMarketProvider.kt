package io.defitrack.market.farming

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.common.utils.Refreshable
import io.defitrack.exit.ExitPositionPreparer
import io.defitrack.market.MarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.token.FungibleToken
import java.math.BigDecimal

abstract class FarmingMarketProvider : MarketProvider<FarmingMarket>() {

    fun create(
        name: String,
        identifier: String,
        stakedToken: FungibleToken,
        rewardTokens: List<FungibleToken>,
        marketSize: Refreshable<BigDecimal>? = null,
        apr: BigDecimal? = null,
        balanceFetcher: PositionFetcher? = null,
        claimableRewardFetcher: ClaimableRewardFetcher? = null,
        investmentPreparer: InvestmentPreparer? = null,
        exitPositionPreparer: ExitPositionPreparer? = null,
        metadata: Map<String, Any> = emptyMap(),
        internalMetadata: Map<String, Any> = emptyMap(),
        rewardsFinished: Boolean = false
    ): FarmingMarket {
        return FarmingMarket(
            id = "frm_${getNetwork().slug}-${getProtocol().slug}-${identifier}",
            network = getNetwork(),
            protocol = getProtocol(),
            name = name,
            stakedToken = stakedToken,
            rewardTokens = rewardTokens,
            marketSize = marketSize,
            apr = apr,
            balanceFetcher = balanceFetcher,
            investmentPreparer = investmentPreparer,
            claimableRewardFetcher = claimableRewardFetcher,
            metadata = metadata,
            internalMetadata = internalMetadata,
            expired = rewardsFinished,
            exitPositionPreparer = exitPositionPreparer
        )
    }
}