package io.defitrack.market.farming

import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.invest.MarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.farming.domain.FarmingPositionFetcher
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.protocol.FarmType
import io.defitrack.token.FungibleToken
import java.math.BigDecimal

abstract class FarmingMarketProvider : MarketProvider<FarmingMarket>() {

    fun create(
        name: String,
        identifier: String,
        stakedToken: FungibleToken,
        rewardTokens: List<FungibleToken>,
        vaultType: String,
        marketSize: BigDecimal? = null,
        apr: BigDecimal? = null,
        balanceFetcher: FarmingPositionFetcher? = null,
        claimableRewardFetcher: ClaimableRewardFetcher? = null,
        investmentPreparer: InvestmentPreparer? = null,
        farmType: FarmType
    ): FarmingMarket {
        return FarmingMarket(
            id = "frm_${getNetwork().slug}-${getProtocol().slug}-${identifier}",
            network = getNetwork(),
            protocol = getProtocol(),
            name = name,
            stakedToken = stakedToken,
            rewardTokens = rewardTokens,
            contractType = vaultType,
            marketSize = marketSize,
            apr = apr,
            farmType = farmType,
            balanceFetcher = balanceFetcher,
            investmentPreparer = investmentPreparer,
            claimableRewardFetcher = claimableRewardFetcher
        )
    }
}