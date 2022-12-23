package io.defitrack.market.farming

import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.invest.MarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.FarmType
import io.defitrack.token.ERC20Resource
import io.defitrack.token.FungibleToken
import java.math.BigDecimal
import java.math.BigInteger

abstract class FarmingMarketProvider : MarketProvider<FarmingMarket>() {

    fun create(
        name: String,
        identifier: String,
        stakedToken: FungibleToken,
        rewardTokens: List<FungibleToken>,
        vaultType: String,
        marketSize: BigDecimal? = null,
        apr: BigDecimal? = null,
        balanceFetcher: PositionFetcher? = null,
        claimableRewardFetcher: ClaimableRewardFetcher? = null,
        investmentPreparer: InvestmentPreparer? = null,
        farmType: FarmType,
        metadata: Map<String, String> = emptyMap()
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
            claimableRewardFetcher = claimableRewardFetcher,
            metadata = metadata
        )
    }

    fun defaultBalanceFetcher(erc20Resource: ERC20Resource, address: String): PositionFetcher {
        return PositionFetcher(
            address,
            { user ->
                erc20Resource.balanceOfFunction(address, user, getNetwork())
            },
            { retVal ->
                retVal[0].value as BigInteger
            }
        )
    }
}