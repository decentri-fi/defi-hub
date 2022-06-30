package io.defitrack.market.farming

import io.defitrack.invest.MarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.farming.domain.FarmingPositionFetcher
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.token.FungibleToken
import java.math.BigDecimal

abstract class FarmingMarketProvider : MarketProvider<FarmingMarket>() {

    fun create(
        id: String,
        name: String,
        stakedToken: FungibleToken,
        rewardTokens: List<FungibleToken>,
        contractAddress: String,
        vaultType: String,
        marketSize: BigDecimal? = null,
        rate: BigDecimal? = null,
        balanceFetcher: FarmingPositionFetcher? = null,
        investmentPreparer: InvestmentPreparer? = null
    ): FarmingMarket {
        return FarmingMarket(
            id = id,
            network = getNetwork(),
            protocol = getProtocol(),
            name = name,
            stakedToken = stakedToken,
            rewardTokens = rewardTokens,
            contractAddress = contractAddress,
            vaultType = vaultType,
            marketSize = marketSize,
            apr = rate,
            balanceFetcher = balanceFetcher,
            investmentPreparer = investmentPreparer
        )
    }
}