package io.defitrack.market.farming

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.common.utils.Refreshable
import io.defitrack.exit.ExitPositionPreparer
import io.defitrack.market.MarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.market.position.PositionFetcher
import io.defitrack.token.FungibleToken
import java.math.BigDecimal

abstract class FarmingMarketProvider : MarketProvider<FarmingMarket>() {

    fun create(
        name: String,
        identifier: String,
        stakedToken: FungibleToken,
        rewardTokens: List<FungibleToken> = emptyList(),
        rewardToken: FungibleToken? = null,
        marketSize: Refreshable<BigDecimal>? = null,
        apr: BigDecimal? = null,
        positionFetcher: PositionFetcher? = null,
        claimableRewardFetchers: List<ClaimableRewardFetcher> = emptyList(),
        claimableRewardFetcher: ClaimableRewardFetcher? = null,
        investmentPreparer: InvestmentPreparer? = null,
        exitPositionPreparer: ExitPositionPreparer? = null,
        metadata: Map<String, Any> = emptyMap(),
        internalMetadata: Map<String, Any> = emptyMap(),
        rewardsFinished: Boolean = false,
        token: FungibleToken? = null,
    ): FarmingMarket {
        return FarmingMarket(
            id = "frm_${getNetwork().slug}-${getProtocol().slug}-${identifier}",
            network = getNetwork(),
            protocol = getProtocol(),
            name = name,
            stakedToken = stakedToken,
            rewardTokens = if (rewardToken != null) rewardTokens + rewardToken else rewardTokens,
            marketSize = marketSize,
            apr = apr,
            balanceFetcher = positionFetcher,
            investmentPreparer = investmentPreparer,
            claimableRewardFetchers = claimableRewardFetchers.let {
                if (claimableRewardFetcher != null) {
                    it + claimableRewardFetcher
                } else {
                    it
                }
            },
            metadata = metadata,
            internalMetadata = internalMetadata,
            expired = rewardsFinished,
            exitPositionPreparer = exitPositionPreparer,
            token = token,
        )
    }
}