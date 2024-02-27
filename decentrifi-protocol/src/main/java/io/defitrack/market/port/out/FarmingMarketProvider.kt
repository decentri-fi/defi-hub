package io.defitrack.market.port.out

import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.common.utils.Refreshable
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.domain.position.ExitPositionPreparer
import io.defitrack.invest.InvestmentPreparer
import io.defitrack.market.domain.farming.FarmingMarket
import java.math.BigDecimal

abstract class FarmingMarketProvider : MarketProvider<FarmingMarket>() {

    /**
     * todo: all market types should also have a type, so we can give an overview of what is implemented
     * todo: move all specific markets to correct layer
     * todo frontend: don't show tokens that are represented as staking markets as well
     */
    fun create(
        name: String,
        identifier: String,
        type: String,
        stakedToken: FungibleTokenInformation,
        rewardTokens: List<FungibleTokenInformation> = emptyList(),
        rewardToken: FungibleTokenInformation? = null,
        marketSize: Refreshable<BigDecimal>? = null,
        apr: BigDecimal? = null,
        positionFetcher: PositionFetcher? = null,
        claimableRewardFetchers: List<ClaimableRewardFetcher> = emptyList(),
        claimableRewardFetcher: ClaimableRewardFetcher? = null,
        investmentPreparer: InvestmentPreparer? = null,
        exitPositionPreparer: ExitPositionPreparer? = null,
        metadata: Map<String, Any> = emptyMap(),
        internalMetadata: Map<String, Any> = emptyMap(),
        deprecated: Boolean = false,
        token: FungibleTokenInformation? = null,
    ): FarmingMarket {
        return FarmingMarket(
            id = "frm_${getNetwork().slug}-${getProtocol().slug}-${identifier}",
            network = getNetwork(),
            protocol = getProtocol(),
            type = type,
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
            deprecated = deprecated,
            exitPositionPreparer = exitPositionPreparer,
            token = token,
        )
    }
}