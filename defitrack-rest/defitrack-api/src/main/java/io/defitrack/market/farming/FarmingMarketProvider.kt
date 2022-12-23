package io.defitrack.market.farming

import io.defitrack.invest.MarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.farming.domain.FarmingPositionFetcher
import io.defitrack.market.farming.domain.InvestmentPreparer
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
        investmentPreparer: InvestmentPreparer? = null,
        underlyingBalanceFetcher: FarmingPositionFetcher? = null,
    ): FarmingMarket {
        return FarmingMarket(
            id = "frm_${getNetwork().slug}-${getProtocol().slug}-${identifier}",
            network = getNetwork(),
            protocol = getProtocol(),
            name = name,
            stakedToken = stakedToken,
            rewardTokens = rewardTokens,
            vaultType = vaultType,
            marketSize = marketSize,
            apr = apr,
            balanceFetcher = balanceFetcher,
            investmentPreparer = investmentPreparer,
            underlyingBalanceFetcher = underlyingBalanceFetcher
        )
    }
}